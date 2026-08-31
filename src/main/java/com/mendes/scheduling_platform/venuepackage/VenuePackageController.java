package com.mendes.scheduling_platform.venuepackage;

import com.mendes.scheduling_platform.exception.*;
import com.mendes.scheduling_platform.plan.PlanService;
import com.mendes.scheduling_platform.security.TenantContext;
import com.mendes.scheduling_platform.venue.VenueRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/venues/{venueId}/packages")
public class VenuePackageController {
    private final VenuePackageRepository repo; private final VenueRepository venues; private final PlanService plans;
    public VenuePackageController(VenuePackageRepository r,VenueRepository v,PlanService p){repo=r;venues=v;plans=p;}
    private Long tenant(){return TenantContext.getRequired();}
    private void venue(Long id){venues.findByIdAndTenantId(id,tenant()).orElseThrow(()->new NotFoundException("Espaço não encontrado"));}
    @GetMapping public List<VenuePackage> list(@PathVariable Long venueId){venue(venueId);return repo.findAllByTenantIdAndVenueId(tenant(),venueId);}
    @PostMapping @PreAuthorize("hasAnyRole('OWNER','MANAGER')") public VenuePackage create(@PathVariable Long venueId,@RequestBody VenuePackage in){
        venue(venueId);
        plans.assertCanCreatePackage(tenant(),venueId);
        if(in.getName()==null||in.getName().isBlank()||in.getDurationMinutes()==null||in.getDurationMinutes()<1||in.getPrice()==null||in.getPrice().signum()<0)throw new BusinessException("Pacote inválido");
        in.setId(null);in.setTenantId(tenant());in.setVenueId(venueId);return repo.save(in);
    }
    @DeleteMapping("/{id}") @PreAuthorize("hasAnyRole('OWNER','MANAGER')") public void delete(@PathVariable Long venueId,@PathVariable Long id){VenuePackage p=repo.findByIdAndTenantIdAndVenueId(id,tenant(),venueId).orElseThrow(()->new NotFoundException("Pacote não encontrado"));repo.delete(p);}
}