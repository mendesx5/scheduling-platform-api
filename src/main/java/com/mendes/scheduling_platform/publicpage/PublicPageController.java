package com.mendes.scheduling_platform.publicpage;

import com.mendes.scheduling_platform.addon.*;
import com.mendes.scheduling_platform.availability.*;
import com.mendes.scheduling_platform.blockedperiod.*;
import com.mendes.scheduling_platform.booking.*;
import com.mendes.scheduling_platform.exception.*;
import com.mendes.scheduling_platform.pricing.PricingService;
import com.mendes.scheduling_platform.tenant.*;
import com.mendes.scheduling_platform.venue.*;
import com.mendes.scheduling_platform.venuepackage.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;

@RestController @RequestMapping("/public/{slug}")
public class PublicPageController {
    private final TenantRepository tenants; private final VenueRepository venues; private final AvailabilityRepository availability;
    private final BookingRepository bookings; private final BlockedPeriodRepository blocked; private final BookingService service;
    private final VenuePackageRepository packages; private final AddonRepository addons; private final PricingService pricing; private final PageSettingsService pageSettings;

    public PublicPageController(TenantRepository t,VenueRepository v,AvailabilityRepository a,BookingRepository b,BlockedPeriodRepository p,
                                BookingService s,VenuePackageRepository packages,AddonRepository addons,PricingService pricing,PageSettingsService pageSettings){
        tenants=t;venues=v;availability=a;bookings=b;blocked=p;service=s;this.packages=packages;this.addons=addons;this.pricing=pricing;this.pageSettings=pageSettings;
    }

    private Tenant tenant(String slug){return tenants.findBySlug(slug).filter(t->t.getStatus()==Tenant.TenantStatus.ACTIVE).orElseThrow(()->new NotFoundException("Estabelecimento não encontrado"));}
    @GetMapping Tenant page(@PathVariable String slug){return tenant(slug);}
    @GetMapping("/page") PageSettingsService.PublicPageData landing(@PathVariable String slug){return pageSettings.publicPage(slug);}
    @GetMapping("/venues") List<Venue> venueList(@PathVariable String slug){return venues.findAllByTenantIdAndActiveTrue(tenant(slug).getId());}
    @GetMapping("/venues/{venueId}/packages") List<VenuePackage> packageList(@PathVariable String slug,@PathVariable Long venueId){Tenant t=tenant(slug);venue(t,venueId);return packages.findAllByTenantIdAndVenueIdAndActiveTrue(t.getId(),venueId);}
    @GetMapping("/venues/{venueId}/addons") List<Addon> addonList(@PathVariable String slug,@PathVariable Long venueId){Tenant t=tenant(slug);venue(t,venueId);return addons.findAllByTenantIdAndVenueIdAndActiveTrue(t.getId(),venueId);}

    @GetMapping("/venues/{venueId}/slots")
    List<OffsetDateTime> slots(@PathVariable String slug,@PathVariable Long venueId,@RequestParam LocalDate date,
                               @RequestParam(defaultValue="-03:00") ZoneOffset offset,@RequestParam(required=false) Integer durationMinutes,
                               @RequestParam(required=false) Integer days,@RequestParam(required=false) Long packageId){
        Tenant t=tenant(slug);Venue v=venue(t,venueId);List<OffsetDateTime> out=new ArrayList<>();
        for(Availability a:availability.findAllByTenantIdAndVenueId(t.getId(),venueId)){
            if(a.getDayOfWeek()!=date.getDayOfWeek())continue;
            int step=slotStep(v);
            for(LocalTime time=a.getStartTime(); !time.isAfter(a.getEndTime()); time=time.plusMinutes(step)){
                OffsetDateTime start=OffsetDateTime.of(date,time,offset);
                if(!start.isAfter(OffsetDateTime.now(offset)))continue;
                try{
                    PricingService.Quote q=pricing.quote(t.getId(),v,start,durationMinutes,days,packageId,List.of());
                    if(v.getPricingType()!=Venue.PricingType.DAILY){
                        if(!q.startDateTime().toLocalDate().equals(date)||!q.endDateTime().toLocalDate().equals(date))continue;
                        if(q.endDateTime().toLocalTime().isAfter(a.getEndTime()))continue;
                    }
                    if(!bookings.existsConflict(t.getId(),venueId,q.startDateTime(),q.endDateTime())
                            &&!blocked.overlaps(t.getId(),venueId,q.startDateTime(),q.endDateTime()))out.add(start);
                }catch(RuntimeException ignored){ }
            }
        }
        return out.stream().distinct().sorted().toList();
    }

    @PostMapping("/quote") PricingService.Quote quote(@PathVariable String slug,@RequestBody BookingService.Request request){return service.quote(tenant(slug).getId(),request);}
    @PostMapping("/bookings") Booking book(@PathVariable String slug,@RequestBody BookingService.Request request){return service.create(tenant(slug).getId(),request);}
    private Venue venue(Tenant t,Long id){return venues.findByIdAndTenantId(id,t.getId()).filter(Venue::isActive).orElseThrow(()->new NotFoundException("Espaço não encontrado"));}
    private int slotStep(Venue v){
        if(v.getPricingType()==Venue.PricingType.HOURLY)return v.getDurationStepMinutes()==null?60:v.getDurationStepMinutes();
        if(v.getPricingType()==Venue.PricingType.DAILY)return 60;
        return v.getSlotDurationMinutes()==null?(v.getDurationMinutes()==null?60:v.getDurationMinutes()):v.getSlotDurationMinutes();
    }
}
