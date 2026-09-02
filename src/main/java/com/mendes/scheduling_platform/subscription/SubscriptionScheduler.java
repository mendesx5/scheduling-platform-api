package com.mendes.scheduling_platform.subscription;
import com.mendes.scheduling_platform.billing.AsaasClient;

import org.springframework.scheduling.annotation.Scheduled; import org.springframework.stereotype.Component;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.Map;
@Component
public class SubscriptionScheduler {
 private final SubscriptionRepository repo; private final AsaasClient asaas;
 public SubscriptionScheduler(SubscriptionRepository repo,AsaasClient asaas){this.repo=repo;this.asaas=asaas;}
 private BigDecimal priceFor(String plan,String cycle){BigDecimal monthly=switch(plan){case "PRO"->new BigDecimal("99.90");case "BUSINESS"->new BigDecimal("169.90");default->new BigDecimal("59.90");};return "YEARLY".equals(cycle)?monthly.multiply(BigDecimal.TEN):monthly;}
 @Scheduled(cron="0 5 0 * * *")
 public void updateSubscriptions(){
   LocalDate today=LocalDate.now();
   repo.findAll().forEach(s->{
     if(s.getPendingPlan()!=null && s.getNextBillingDate()!=null && !s.getNextBillingDate().isAfter(today) && s.getAsaasSubscriptionId()!=null){
       String plan=s.getPendingPlan();String cycle=s.getPendingBillingCycle()==null?s.getBillingCycle():s.getPendingBillingCycle();
       asaas.updateSubscription(s.getAsaasSubscriptionId(),Map.of("value",priceFor(plan,cycle),"cycle",cycle));
       s.setPlan(plan);s.setBillingCycle(cycle);s.setPendingPlan(null);s.setPendingBillingCycle(null);repo.save(s);
     }
     if(s.getStatus()==Subscription.Status.ACTIVE && s.getNextBillingDate()!=null && s.getNextBillingDate().isBefore(today)){s.setStatus(Subscription.Status.PAST_DUE);repo.save(s);}
   });
 }
}
