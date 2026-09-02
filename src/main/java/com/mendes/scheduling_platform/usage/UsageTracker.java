package com.mendes.scheduling_platform.usage;
import com.mendes.scheduling_platform.booking.BookingRepository; import com.mendes.scheduling_platform.customer.CustomerRepository; import com.mendes.scheduling_platform.security.TenantContext; import com.mendes.scheduling_platform.user.UserRepository;
import org.springframework.scheduling.annotation.Scheduled; import org.springframework.stereotype.Service; import java.time.LocalDate; import java.util.concurrent.ConcurrentHashMap; import java.util.concurrent.atomic.LongAdder;
@Service
public class UsageTracker {
 private record Key(Long tenant,LocalDate date){} private final ConcurrentHashMap<Key,LongAdder> counters=new ConcurrentHashMap<>(); private final TenantUsageDailyRepository repo; private final UserRepository users;
 public UsageTracker(TenantUsageDailyRepository r,UserRepository u){repo=r;users=u;}
 public void request(){Long tenant=TenantContext.get();if(tenant==null)return;counters.computeIfAbsent(new Key(tenant,LocalDate.now()),k->new LongAdder()).increment();}
 @Scheduled(fixedDelay=60000) public void flush(){counters.forEach((key,adder)->{TenantUsageDaily row=repo.findByTenantIdAndUsageDate(key.tenant(),key.date()).orElseGet(()->{var x=new TenantUsageDaily();x.setTenantId(key.tenant());x.setUsageDate(key.date());return x;});row.setApiRequests(row.getApiRequests()+adder.sumThenReset());row.setActiveUsers(users.countByTenantIdAndActiveTrue(key.tenant()));repo.save(row);});}
}
