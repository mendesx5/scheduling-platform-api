package com.mendes.scheduling_platform.bookingpolicy;

import com.mendes.scheduling_platform.exception.*;
import com.mendes.scheduling_platform.security.TenantContext;
import com.mendes.scheduling_platform.venue.VenueRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/venues/{venueId}/policy")
public class VenueBookingPolicyController {
    private final VenueBookingPolicyRepository repo; private final VenueRepository venues;
    public VenueBookingPolicyController(VenueBookingPolicyRepository r,VenueRepository v){repo=r;venues=v;}
    private Long tenant(){return TenantContext.getRequired();}
    private void venue(Long id){venues.findByIdAndTenantId(id,tenant()).orElseThrow(()->new NotFoundException("Espaço não encontrado"));}
    @GetMapping public VenueBookingPolicy get(@PathVariable Long venueId){venue(venueId);return repo.findByTenantIdAndVenueId(tenant(),venueId).orElseGet(()->defaults(venueId));}
    @PutMapping @PreAuthorize("hasAnyRole('OWNER','MANAGER')") public VenueBookingPolicy save(@PathVariable Long venueId,@RequestBody VenueBookingPolicy in){venue(venueId);if(in.getMinimumAdvanceMinutes()==null||in.getMinimumAdvanceMinutes()<0||in.getMaximumAdvanceDays()==null||in.getMaximumAdvanceDays()<1||in.getCancellationDeadlineHours()==null||in.getCancellationDeadlineHours()<0)throw new BusinessException("Política de reserva inválida");VenueBookingPolicy p=repo.findByTenantIdAndVenueId(tenant(),venueId).orElseGet(()->defaults(venueId));p.setRequiresApproval(in.isRequiresApproval());p.setMinimumAdvanceMinutes(in.getMinimumAdvanceMinutes());p.setMaximumAdvanceDays(in.getMaximumAdvanceDays());p.setCancellationAllowed(in.isCancellationAllowed());p.setCancellationDeadlineHours(in.getCancellationDeadlineHours());return repo.save(p);}
    private VenueBookingPolicy defaults(Long venueId){VenueBookingPolicy p=new VenueBookingPolicy();p.setTenantId(tenant());p.setVenueId(venueId);return p;}
}
