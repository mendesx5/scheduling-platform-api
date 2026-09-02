package com.mendes.scheduling_platform.dashboard;

import com.mendes.scheduling_platform.booking.Booking;
import com.mendes.scheduling_platform.booking.BookingRepository;
import com.mendes.scheduling_platform.customer.CustomerRepository;
import com.mendes.scheduling_platform.plan.PlanService;
import com.mendes.scheduling_platform.security.TenantContext;
import com.mendes.scheduling_platform.venue.VenueRepository;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.math.RoundingMode; import java.time.*; import java.util.*;

@RestController @RequestMapping("/dashboard")
public class DashboardController {
 private final BookingRepository bookings; private final CustomerRepository customers; private final VenueRepository venues; private final PlanService plans;
 public DashboardController(BookingRepository b,CustomerRepository c,VenueRepository v,PlanService p){bookings=b;customers=c;venues=v;plans=p;}
 public record Point(String date,long bookings,BigDecimal revenue,long customers){}
 public record Analytics(String period,LocalDate start,LocalDate end,long reservations,BigDecimal revenue,long customers,long activeVenues,double occupationRate,List<Point> points,Long previousReservations,BigDecimal previousRevenue){}
 @GetMapping("/analytics")
 Analytics analytics(@RequestParam(defaultValue="WEEK") String period){
   Long tenant=TenantContext.getRequired(); boolean monthly=period.equalsIgnoreCase("MONTH"); if(monthly) plans.assertFeature(tenant,PlanService.Feature.PLUS_ANALYTICS); else plans.assertFeature(tenant,PlanService.Feature.ADVANCED_DASHBOARD);
   LocalDate end=LocalDate.now(), start=monthly?end.minusDays(29):end.minusDays(6); return build(tenant,start,end,period);
 }
 private Analytics build(Long tenant,LocalDate start,LocalDate end,String period){
   OffsetDateTime from=start.atStartOfDay().atOffset(ZoneOffset.UTC), to=end.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
   List<Booking> list=bookings.findAllByTenantIdAndStartDateTimeBetween(tenant,from,to);
   long reservations=list.stream().filter(b->b.getStatus()!=Booking.Status.CANCELLED).count();
   BigDecimal revenue=list.stream().filter(b->b.getPaymentStatus()==Booking.PaymentStatus.PAID).map(b->b.getTotalAmount()==null?BigDecimal.ZERO:b.getTotalAmount()).reduce(BigDecimal.ZERO,BigDecimal::add);
   long customerCount=customers.findAllByTenantId(tenant).size(), activeVenues=venues.countByTenantIdAndActiveTrue(tenant);
   Map<LocalDate,List<Booking>> grouped=new LinkedHashMap<>(); for(LocalDate d=start;!d.isAfter(end);d=d.plusDays(1))grouped.put(d,new ArrayList<>());
   list.forEach(b->{LocalDate d=b.getStartDateTime().toLocalDate();if(grouped.containsKey(d))grouped.get(d).add(b);});
   List<Point> points=new ArrayList<>(); for(var e:grouped.entrySet()){var bs=e.getValue();long count=bs.stream().filter(b->b.getStatus()!=Booking.Status.CANCELLED).count();BigDecimal rev=bs.stream().filter(b->b.getPaymentStatus()==Booking.PaymentStatus.PAID).map(b->b.getTotalAmount()==null?BigDecimal.ZERO:b.getTotalAmount()).reduce(BigDecimal.ZERO,BigDecimal::add);points.add(new Point(e.getKey().toString(),count,rev,bs.stream().map(Booking::getCustomerId).distinct().count()));}
   double bookedHours=list.stream().filter(b->b.getStatus()!=Booking.Status.CANCELLED).mapToDouble(b->Duration.between(b.getStartDateTime(),b.getEndDateTime()).toMinutes()/60d).sum();
   double capacity=Math.max(1,activeVenues*12d*((end.toEpochDay()-start.toEpochDay())+1)); double occupation=Math.min(100,bookedHours/capacity*100);
   LocalDate prevEnd=start.minusDays(1), prevStart=prevEnd.minusDays((end.toEpochDay()-start.toEpochDay())); List<Booking> prev=bookings.findAllByTenantIdAndStartDateTimeBetween(tenant,prevStart.atStartOfDay().atOffset(ZoneOffset.UTC),prevEnd.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC));
   Long pr=prev.stream().filter(b->b.getStatus()!=Booking.Status.CANCELLED).count(); BigDecimal prevRev=prev.stream().filter(b->b.getPaymentStatus()==Booking.PaymentStatus.PAID).map(b->b.getTotalAmount()==null?BigDecimal.ZERO:b.getTotalAmount()).reduce(BigDecimal.ZERO,BigDecimal::add);
   return new Analytics(period,start,end,reservations,revenue,customerCount,activeVenues,BigDecimal.valueOf(occupation).setScale(1,RoundingMode.HALF_UP).doubleValue(),points,pr,prevRev);
 }
}
