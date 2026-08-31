package com.mendes.scheduling_platform.subscription;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
@Component
public class SubscriptionScheduler {
 private final SubscriptionRepository repo;
 public SubscriptionScheduler(SubscriptionRepository repo){this.repo=repo;}
 /** Marks overdue subscriptions. Payment gateway integration can later reactivate after webhook confirmation. */
 @Scheduled(cron="0 5 0 * * *")
 public void updateOverdueSubscriptions(){ LocalDate today=LocalDate.now(); repo.findAll().forEach(s->{ if(s.getStatus()==Subscription.Status.ACTIVE && s.getNextBillingDate()!=null && s.getNextBillingDate().isBefore(today)){s.setStatus(Subscription.Status.PAST_DUE);repo.save(s);} }); }
}
