package com.mendes.scheduling_platform.tenant;

import com.mendes.scheduling_platform.exception.*;
import com.mendes.scheduling_platform.subscription.*;
import com.mendes.scheduling_platform.user.*;
import com.mendes.scheduling_platform.billing.AsaasBillingService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;

@Service
public class TenantService {
    private final TenantRepository tenants;
    private final UserRepository users;
    private final SubscriptionRepository subscriptions;
    private final PasswordEncoder encoder;
    private final AsaasBillingService billing;

    public TenantService(TenantRepository t, UserRepository u, SubscriptionRepository s,
                         PasswordEncoder e, AsaasBillingService billing) {
        tenants=t; users=u; subscriptions=s; encoder=e; this.billing=billing;
    }

    public record Registration(String name,String slug,String ownerName,String email,String password,String phone,String address,String plan){}
    public record RegistrationResult(Long tenantId,String checkoutId,String checkoutUrl,String plan){}

    @Transactional
    public RegistrationResult register(Registration r){
        String slug=r.slug().trim().toLowerCase();
        String plan=billing.normalizePlan(r.plan());
        if(tenants.existsBySlug(slug)) throw new BusinessException("Slug já utilizado");

        Tenant t=new Tenant();
        t.setName(r.name()); t.setSlug(slug); t.setPhone(r.phone()); t.setAddress(r.address());
        t.setPlan(plan); t.setStatus(Tenant.TenantStatus.PENDING_PAYMENT);
        t=tenants.save(t);

        User u=new User();
        u.setTenantId(t.getId()); u.setName(r.ownerName()); u.setEmail(r.email().toLowerCase());
        u.setPassword(encoder.encode(r.password())); u.setRole(User.Role.OWNER); users.save(u);

        Subscription s=new Subscription();
        s.setTenantId(t.getId()); s.setPlan(plan); s.setStatus(Subscription.Status.PAYMENT_PENDING);
        subscriptions.save(s);

        var checkout=billing.createCheckout(t, s, r.ownerName(), r.email(), r.phone(), r.address());
        s.setAsaasCheckoutId(checkout.id()); subscriptions.save(s);

        return new RegistrationResult(t.getId(), checkout.id(), checkout.url(), plan);
    }
}
