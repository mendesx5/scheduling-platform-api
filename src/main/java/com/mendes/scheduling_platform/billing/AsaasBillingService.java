package com.mendes.scheduling_platform.billing;

import com.fasterxml.jackson.databind.JsonNode;
import com.mendes.scheduling_platform.exception.BusinessException;
import com.mendes.scheduling_platform.subscription.Subscription;
import com.mendes.scheduling_platform.subscription.SubscriptionRepository;
import com.mendes.scheduling_platform.tenant.Tenant;
import com.mendes.scheduling_platform.tenant.TenantRepository;
import com.mendes.scheduling_platform.user.User;
import com.mendes.scheduling_platform.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Service
public class AsaasBillingService {
    private final AsaasClient client;
    private final SubscriptionRepository subscriptions;
    private final TenantRepository tenants;
    private final AsaasWebhookEventRepository webhookEvents;
    private final UserRepository users;

    public AsaasBillingService(AsaasClient client, SubscriptionRepository subscriptions, TenantRepository tenants, AsaasWebhookEventRepository webhookEvents, UserRepository users) {
        this.client=client; this.subscriptions=subscriptions; this.tenants=tenants; this.webhookEvents=webhookEvents; this.users=users;
    }

    public record Checkout(String id,String url){}
    public record CheckoutStatus(String status,String plan){}

    public String normalizePlan(String plan) {
        String value = plan == null ? "STARTER" : plan.trim().toUpperCase();
        if (!Map.of("STARTER",59.90,"PRO",99.90,"BUSINESS",169.90).containsKey(value)) {
            throw new BusinessException("Plano inválido");
        }
        return value;
    }

    public String normalizeCycle(String cycle) {
        String value = cycle == null || cycle.isBlank() ? "MONTHLY" : cycle.trim().toUpperCase();
        if (!value.equals("MONTHLY") && !value.equals("YEARLY")) {
            throw new BusinessException("Ciclo de cobrança inválido");
        }
        return value;
    }

    public BigDecimal priceFor(String plan) {
        return switch (normalizePlan(plan)) {
            case "STARTER" -> new BigDecimal("59.90");
            case "PRO" -> new BigDecimal("99.90");
            case "BUSINESS" -> new BigDecimal("169.90");
            default -> throw new BusinessException("Plano inválido");
        };
    }

    // Ciclo anual equivale a 10x o valor mensal (2 meses de desconto).
    public BigDecimal annualPriceFor(String plan) {
        return priceFor(plan).multiply(BigDecimal.TEN);
    }

    public BigDecimal priceFor(String plan, String cycle) {
        return normalizeCycle(cycle).equals("YEARLY") ? annualPriceFor(plan) : priceFor(plan);
    }

    @Transactional
    public Checkout createCheckout(Tenant tenant, Subscription subscription, String ownerName, String email, String phone, String address) {
        String cycle=normalizeCycle(subscription.getBillingCycle());
        BigDecimal price=priceFor(subscription.getPlan(), cycle);
        JsonNode response=client.createRecurringCheckout(Map.of(
            "billingTypes", new String[]{"CREDIT_CARD","PIX","BOLETO"},
            "chargeTypes", new String[]{"RECURRENT"},
            "minutesToExpire", 60,
            "externalReference", subscription.getId().toString(),
            "items", new Object[]{Map.of(
                "name", "AgendaHub - Plano " + subscription.getPlan(),
                "description", cycle.equals("YEARLY") ? "Assinatura anual do AgendaHub" : "Assinatura mensal do AgendaHub",
                "quantity", 1,
                "value", price
            )},
            "subscription", Map.of(
                "cycle", cycle,
                "nextDueDate", LocalDate.now().toString()
            ),
            "callback", Map.of(
                "successUrl", client.frontendUrl() + "/billing/success",
                "cancelUrl", client.frontendUrl() + "/billing/cancelled",
                "expiredUrl", client.frontendUrl() + "/billing/expired"
            )
        ));
        String id=text(response,"id");
        if(id==null) throw new BusinessException("Asaas não retornou o ID do checkout");
        String link=text(response,"link");
        if(link==null) throw new BusinessException("Asaas não retornou o link do checkout");
        return new Checkout(id, link);
    }

    @Transactional
    public void handleWebhook(String event, JsonNode payload) {
        String eventId=payload.path("id").asText(null);
        if(eventId==null || eventId.isBlank()) throw new BusinessException("Webhook sem identificador");
        if(webhookEvents.existsById(eventId)) return;
        AsaasWebhookEvent record=new AsaasWebhookEvent(); record.setId(eventId); record.setEvent(event); webhookEvents.save(record);
        switch (event) {
            case "CHECKOUT_PAID" -> handleCheckoutPaid(payload.path("checkout"));
            case "CHECKOUT_CANCELED", "CHECKOUT_EXPIRED" -> handleCheckoutNotPaid(payload.path("checkout"));
            case "SUBSCRIPTION_CREATED", "SUBSCRIPTION_UPDATED" -> handleSubscription(payload.path("subscription"));
            case "PAYMENT_RECEIVED", "PAYMENT_CONFIRMED" -> handlePayment(payload.path("payment"), true);
            case "PAYMENT_OVERDUE" -> handlePayment(payload.path("payment"), false);
            case "PAYMENT_REFUNDED", "PAYMENT_DELETED" -> handlePayment(payload.path("payment"), false);
            default -> { }
        }
    }

    private void handleCheckoutPaid(JsonNode checkout) {
        Subscription s=findByCheckout(checkout.path("id").asText(null));
        if(s==null) return;
        if(s.getPendingPlan()!=null) {
            if(s.getAsaasSubscriptionId()!=null) {
                s.setPreviousAsaasSubscriptionId(s.getAsaasSubscriptionId());
                client.cancelSubscription(s.getAsaasSubscriptionId());
            }
            s.setPlan(s.getPendingPlan());
            if(s.getPendingBillingCycle()!=null) s.setBillingCycle(s.getPendingBillingCycle());
            s.setPendingPlan(null); s.setPendingBillingCycle(null);
        }
        s.setStatus(Subscription.Status.ACTIVE);
        s.setStartDate(LocalDate.now());
        s.setNextBillingDate(nextBillingDate(LocalDate.now(), s.getBillingCycle()));
        if(checkout.hasNonNull("customer")) s.setAsaasCustomerId(checkout.get("customer").asText());
        subscriptions.save(s);
        activateTenant(s);
    }

    private void handleCheckoutNotPaid(JsonNode checkout) {
        Subscription s=findByCheckout(checkout.path("id").asText(null));
        if(s==null || s.getStatus()==Subscription.Status.ACTIVE) return;
        s.setStatus(Subscription.Status.PAYMENT_PENDING); subscriptions.save(s);
    }

    private void handleSubscription(JsonNode subscription) {
        String external=subscription.path("externalReference").asText(null);
        Subscription s=null;
        if(external!=null){ try { s=subscriptions.findById(Long.valueOf(external)).orElse(null); } catch(NumberFormatException ignored) {} }
        if(s==null) {
            String asaasId=subscription.path("id").asText(null);
            s=asaasId==null?null:subscriptions.findByAsaasSubscriptionId(asaasId).orElse(null);
        }
        if(s==null) return;
        s.setAsaasSubscriptionId(subscription.path("id").asText(null));
        if(subscription.hasNonNull("customer")) s.setAsaasCustomerId(subscription.get("customer").asText());
        String due=subscription.path("nextDueDate").asText(null);
        if(due!=null && due.length()>=10) s.setNextBillingDate(LocalDate.parse(due.substring(0,10)));
        subscriptions.save(s);
    }

    private void handlePayment(JsonNode payment, boolean paid) {
        String asaasSubscriptionId=payment.path("subscription").asText(null);
        if(asaasSubscriptionId==null || asaasSubscriptionId.isBlank()) return;
        Subscription s=subscriptions.findByAsaasSubscriptionId(asaasSubscriptionId).orElse(null);
        if(s==null) return;
        if(paid) {
            s.setStatus(Subscription.Status.ACTIVE);
            s.setLastBillingDate(LocalDate.now());
            String due=payment.path("dueDate").asText(null);
            LocalDate base=(due!=null && due.length()>=10) ? LocalDate.parse(due.substring(0,10)) : LocalDate.now();
            s.setNextBillingDate(nextBillingDate(base, s.getBillingCycle()));
            subscriptions.save(s); activateTenant(s);
        } else {
            s.setStatus(Subscription.Status.PAST_DUE); subscriptions.save(s);
        }
    }

    private void activateTenant(Subscription s) {
        tenants.findById(s.getTenantId()).ifPresent(t->{t.setStatus(Tenant.TenantStatus.ACTIVE);t.setPlan(s.getPlan());tenants.save(t);});
    }

    private Subscription findByCheckout(String checkoutId) {
        if(checkoutId==null || checkoutId.isBlank()) return null;
        return subscriptions.findByAsaasCheckoutId(checkoutId).orElse(null);
    }

    public CheckoutStatus checkoutStatus(String checkoutId) {
        Subscription s=findByCheckout(checkoutId);
        if(s==null) throw new BusinessException("Checkout não encontrado");
        return new CheckoutStatus(s.getStatus().name(), s.getPlan());
    }

    @Transactional
    public void cancel(Long tenantId) {
        Subscription s=subscriptions.findByTenantId(tenantId).orElseThrow(()->new BusinessException("Assinatura não encontrada"));
        if(s.getAsaasSubscriptionId()!=null) client.cancelSubscription(s.getAsaasSubscriptionId());
        s.setStatus(Subscription.Status.CANCELLED); subscriptions.save(s);
        tenants.findById(tenantId).ifPresent(t->{t.setStatus(Tenant.TenantStatus.SUSPENDED);tenants.save(t);});
    }

    private int rank(String plan){return switch(normalizePlan(plan)){case "STARTER"->1;case "PRO"->2;case "BUSINESS"->3;default->1;}}

    private String text(JsonNode node,String field){return node.hasNonNull(field)?node.get(field).asText():null;}
    private LocalDate nextBillingDate(LocalDate from,String cycle){return "YEARLY".equals(cycle)?from.plusYears(1):from.plusMonths(1);}
}
