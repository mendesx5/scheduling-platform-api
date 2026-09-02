package com.mendes.scheduling_platform.platform;

import com.mendes.scheduling_platform.booking.BookingRepository; import com.mendes.scheduling_platform.exception.NotFoundException;
import com.mendes.scheduling_platform.subscription.*; import com.mendes.scheduling_platform.tenant.*; import com.mendes.scheduling_platform.usage.*; import com.mendes.scheduling_platform.user.User; import com.mendes.scheduling_platform.user.UserRepository;
import org.springframework.web.bind.annotation.*; import org.springframework.security.core.Authentication;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.*;

@RestController @RequestMapping("/platform/admin")
public class PlatformAdminController {
 private final TenantRepository tenants; private final SubscriptionRepository subscriptions; private final BookingRepository bookings; private final TenantUsageDailyRepository usage; private final PlatformAdminRepository admins; private final UserRepository users; private final AdminAuditLogRepository audit;
 public PlatformAdminController(TenantRepository t,SubscriptionRepository s,BookingRepository b,TenantUsageDailyRepository u,PlatformAdminRepository admins,AdminAuditLogRepository audit,UserRepository users){tenants=t;subscriptions=s;bookings=b;usage=u;this.admins=admins;this.audit=audit;this.users=users;}
 public record Metrics(long tenants,long activeTenants,long trialTenants,long payingTenants,long bookings,BigDecimal grossBookingValue,BigDecimal mrr,long apiRequests30d){}
 @GetMapping("/tenants") List<Tenant> tenants(){return tenants.findAll();}
 @GetMapping("/metrics") Metrics metrics(){
   List<Tenant> ts=tenants.findAll(); long active=ts.stream().filter(t->t.getStatus()==Tenant.TenantStatus.ACTIVE).count();
   long trial=subscriptions.findAll().stream().filter(s->s.getStatus()==Subscription.Status.TRIAL).count();
   long paying=subscriptions.findAll().stream().filter(s->s.getStatus()==Subscription.Status.ACTIVE||s.getStatus()==Subscription.Status.TRIAL).count();
   BigDecimal gross=bookings.findAll().stream().map(x->x.getTotalAmount()==null?BigDecimal.ZERO:x.getTotalAmount()).reduce(BigDecimal.ZERO,BigDecimal::add);
   BigDecimal mrr=subscriptions.findAll().stream().filter(s->s.getStatus()==Subscription.Status.ACTIVE||s.getStatus()==Subscription.Status.TRIAL).map(s->monthlyPrice(s.getPlan(),s.getBillingCycle())).reduce(BigDecimal.ZERO,BigDecimal::add);
   LocalDate since=LocalDate.now().minusDays(29); long req=usage.findAll().stream().filter(x->!x.getUsageDate().isBefore(since)).mapToLong(TenantUsageDaily::getApiRequests).sum();
   return new Metrics(ts.size(),active,trial,paying,bookings.count(),gross,mrr,req);
 }
 public record UsagePoint(String date,long requests,long bookingsCreated,long bookingsCancelled,long activeUsers,long customersCreated,BigDecimal revenue){}
 @GetMapping("/usage") List<UsagePoint> usage(@RequestParam(defaultValue="30") int days){
   LocalDate start=LocalDate.now().minusDays(Math.max(1,Math.min(days,365))-1); Map<LocalDate,List<TenantUsageDaily>> grouped=new LinkedHashMap<>();
   for(LocalDate d=start;!d.isAfter(LocalDate.now());d=d.plusDays(1))grouped.put(d,new ArrayList<>());
   usage.findAll().stream().filter(x->!x.getUsageDate().isBefore(start)).forEach(x->grouped.computeIfAbsent(x.getUsageDate(),k->new ArrayList<>()).add(x));
   return grouped.entrySet().stream().map(e->new UsagePoint(e.getKey().toString(),e.getValue().stream().mapToLong(TenantUsageDaily::getApiRequests).sum(),e.getValue().stream().mapToLong(TenantUsageDaily::getBookingsCreated).sum(),e.getValue().stream().mapToLong(TenantUsageDaily::getBookingsCancelled).sum(),e.getValue().stream().mapToLong(TenantUsageDaily::getActiveUsers).sum(),e.getValue().stream().mapToLong(TenantUsageDaily::getCustomersCreated).sum(),e.getValue().stream().map(TenantUsageDaily::getRevenue).reduce(BigDecimal.ZERO,BigDecimal::add))).toList();
 }
 public record Alert(Long tenantId,String tenantName,String type,String message,String recommendation){}
 @GetMapping("/alerts") List<Alert> alerts(){
   LocalDate since=LocalDate.now().minusDays(6); List<Alert> out=new ArrayList<>();
   for(Tenant t:tenants.findAll()){var rows=usage.findAllByTenantIdAndUsageDateBetweenOrderByUsageDate(t.getId(),since,LocalDate.now());long totalReq=rows.stream().mapToLong(TenantUsageDaily::getApiRequests).sum();long avg=Math.max(1,rows.size()>1?totalReq/rows.size():100);long peak=rows.stream().mapToLong(TenantUsageDaily::getApiRequests).max().orElse(0);if(peak>avg*4)out.add(new Alert(t.getId(),t.getName(),"HIGH_USAGE","Pico diário de requisições acima do padrão.","Avaliar uso e oferecer upgrade se o crescimento for legítimo."));if(t.getPlan().equals("PRO")&&rows.stream().mapToLong(TenantUsageDaily::getBookingsCreated).sum()>40)out.add(new Alert(t.getId(),t.getName(),"UPGRADE","Operação crescendo rapidamente.","Considerar oferecer o plano Plus."));}
   return out;
 }
 @PatchMapping("/tenants/{id}/suspension") Tenant suspension(@PathVariable Long id,@RequestParam boolean suspended,Authentication a){Tenant t=tenant(id);t.setStatus(suspended?Tenant.TenantStatus.SUSPENDED:Tenant.TenantStatus.ACTIVE);Tenant saved=tenants.save(t);log(a,id,suspended?"SUSPEND_TENANT":"REACTIVATE_TENANT","");return saved;}
 public record TenantUpdate(String name,String slug,String phone,String instagram,String address,String plan){}
 @PutMapping("/tenants/{id}") Tenant update(@PathVariable Long id,@RequestBody TenantUpdate in,Authentication a){Tenant t=tenant(id);if(in.name()!=null)t.setName(in.name());if(in.slug()!=null&&!in.slug().equals(t.getSlug())){if(tenants.existsBySlug(in.slug()))throw new IllegalArgumentException("Slug já utilizado");t.setSlug(in.slug().toLowerCase());}t.setPhone(in.phone());t.setInstagram(in.instagram());t.setAddress(in.address());if(in.plan()!=null)t.setPlan(in.plan().toUpperCase());Tenant saved=tenants.save(t);log(a,id,"EDIT_TENANT",in.toString());return saved;}
 @PutMapping("/tenants/{id}/subscription") Subscription subscription(@PathVariable Long id,@RequestBody Subscription in,Authentication a){Tenant t=tenant(id);Subscription s=subscriptions.findByTenantId(id).orElseGet(Subscription::new);s.setTenantId(id);s.setPlan(in.getPlan());s.setStatus(in.getStatus());s.setStartDate(in.getStartDate());s.setEndDate(in.getEndDate());s.setBillingCycle(in.getBillingCycle()==null?"MONTHLY":in.getBillingCycle());t.setPlan(in.getPlan());tenants.save(t);Subscription saved=subscriptions.save(s);log(a,id,"EDIT_SUBSCRIPTION","plan="+in.getPlan());return saved;}
 public record OwnerUpdate(String name,String email,String username){}
 @PutMapping("/tenants/{id}/owner") User owner(@PathVariable Long id,@RequestBody OwnerUpdate in,Authentication a){tenant(id);User u=users.findAllByTenantId(id).stream().filter(x->x.getRole()==User.Role.OWNER).findFirst().orElseThrow(()->new NotFoundException("Proprietário não encontrado"));if(in.name()!=null)u.setName(in.name());if(in.email()!=null)u.setEmail(in.email().toLowerCase());if(in.username()!=null)u.setUsername(in.username().toLowerCase());User saved=users.save(u);log(a,id,"EDIT_OWNER","email="+u.getEmail());return saved;}
 @GetMapping("/tenants/{id}/audit") List<AdminAuditLog> audit(@PathVariable Long id){tenant(id);return audit.findAllByTenantIdOrderByCreatedAtDesc(id);}
 @GetMapping("/tenants/{id}/usage") List<TenantUsageDaily> tenantUsage(@PathVariable Long id,@RequestParam(defaultValue="30") int days){tenant(id);LocalDate s=LocalDate.now().minusDays(days-1);return usage.findAllByTenantIdAndUsageDateBetweenOrderByUsageDate(id,s,LocalDate.now());}
 private Tenant tenant(Long id){return tenants.findById(id).orElseThrow(()->new NotFoundException("Tenant não encontrado"));}
 private BigDecimal monthlyPrice(String plan,String cycle){BigDecimal m=switch(plan==null?"STARTER":plan){case "PRO"->new BigDecimal("99.90");case "BUSINESS"->new BigDecimal("169.90");default->new BigDecimal("59.90");};return "YEARLY".equals(cycle)?m.divide(BigDecimal.ONE,2,java.math.RoundingMode.HALF_UP).multiply(BigDecimal.TEN).divide(BigDecimal.valueOf(12),2,java.math.RoundingMode.HALF_UP):m;}
 private void log(Authentication a,Long tenantId,String action,String details){admins.findByEmailIgnoreCase(a.getName()).ifPresent(ad->{var l=new AdminAuditLog();l.setAdminId(ad.getId());l.setTenantId(tenantId);l.setAction(action);l.setDetails(details);audit.save(l);});}
}
