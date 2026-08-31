package com.mendes.scheduling_platform.addon;

import com.mendes.scheduling_platform.exception.*;
import com.mendes.scheduling_platform.plan.PlanService;
import com.mendes.scheduling_platform.security.TenantContext;
import com.mendes.scheduling_platform.venue.VenueRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/venues/{venueId}/addons")
public class AddonController {
    private final AddonRepository repo; private final VenueRepository venues; private final PlanService plans;
    public AddonController(AddonRepository r,VenueRepository v,PlanService p){repo=r;venues=v;plans=p;}
    private Long tenant(){return TenantContext.getRequired();}
    private void venue(Long id){venues.findByIdAndTenantId(id,tenant()).orElseThrow(()->new NotFoundException("Espaço não encontrado"));}
    @GetMapping public List<Addon> list(@PathVariable Long venueId){venue(venueId);return repo.findAllByTenantIdAndVenueId(tenant(),venueId);}
    @PostMapping @PreAuthorize("hasAnyRole('OWNER','MANAGER')") public Addon create(@PathVariable Long venueId,@RequestBody Addon in){
        venue(venueId);
        plans.assertCanCreateAddon(tenant(),venueId);
        if(in.getName()==null||in.getName().isBlank()||in.getPrice()==null||in.getPrice().signum()<0)throw new BusinessException("Adicional inválido");
        in.setId(null);in.setTenantId(tenant());in.setVenueId(venueId);return repo.save(in);
    }
    @DeleteMapping("/{id}") @PreAuthorize("hasAnyRole('OWNER','MANAGER')") public void delete(@PathVariable Long venueId,@PathVariable Long id){Addon a=repo.findByIdAndTenantIdAndVenueId(id,tenant(),venueId).orElseThrow(()->new NotFoundException("Adicional não encontrado"));repo.delete(a);}
}