package com.mendes.scheduling_platform.usage;
import org.springframework.stereotype.Service; import java.math.BigDecimal; import java.time.LocalDate;
@Service
public class UsageEventService {
 private final TenantUsageDailyRepository repo; public UsageEventService(TenantUsageDailyRepository r){repo=r;}
 public void bookingCreated(Long tenant){var r=row(tenant);r.setBookingsCreated(r.getBookingsCreated()+1);repo.save(r);}
 public void bookingCancelled(Long tenant){var r=row(tenant);r.setBookingsCancelled(r.getBookingsCancelled()+1);repo.save(r);}
 public void customerCreated(Long tenant){var r=row(tenant);r.setCustomersCreated(r.getCustomersCreated()+1);repo.save(r);}
 public void revenue(Long tenant,BigDecimal value){var r=row(tenant);r.setRevenue(r.getRevenue().add(value==null?BigDecimal.ZERO:value));repo.save(r);}
 private TenantUsageDaily row(Long tenant){return repo.findByTenantIdAndUsageDate(tenant,LocalDate.now()).orElseGet(()->{var x=new TenantUsageDaily();x.setTenantId(tenant);x.setUsageDate(LocalDate.now());x.setRevenue(BigDecimal.ZERO);return x;});}
 private void save(Long tenant){repo.findByTenantIdAndUsageDate(tenant,LocalDate.now()).ifPresent(repo::save);}
}
