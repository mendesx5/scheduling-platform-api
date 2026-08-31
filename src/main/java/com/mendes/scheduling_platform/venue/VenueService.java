package com.mendes.scheduling_platform.venue;

import com.mendes.scheduling_platform.exception.*;
import com.mendes.scheduling_platform.plan.PlanService;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;

@Service
public class VenueService {
    private final VenueRepository repo; private final PlanService plans;
    public VenueService(VenueRepository r,PlanService p){repo=r;plans=p;}
    public List<Venue> list(Long t){return repo.findAllByTenantId(t);}
    public Venue get(Long t,Long id){return repo.findByIdAndTenantId(id,t).orElseThrow(()->new NotFoundException("Espaço não encontrado"));}
    public Venue save(Long t,Venue v){plans.assertCanCreateVenue(t);validate(v);normalize(v);v.setId(null);v.setTenantId(t);return repo.save(v);}
    public Venue update(Long t,Long id,Venue in){validate(in);Venue v=get(t,id);copy(v,in);normalize(v);return repo.save(v);}
    public void delete(Long t,Long id){repo.delete(get(t,id));}
    private void copy(Venue v,Venue i){v.setName(i.getName());v.setDescription(i.getDescription());v.setType(i.getType());v.setPrice(i.getPrice());v.setDurationMinutes(i.getDurationMinutes());v.setPricingType(i.getPricingType());v.setBasePrice(i.getBasePrice());v.setSlotDurationMinutes(i.getSlotDurationMinutes());v.setMinimumDurationMinutes(i.getMinimumDurationMinutes());v.setMaximumDurationMinutes(i.getMaximumDurationMinutes());v.setDurationStepMinutes(i.getDurationStepMinutes());v.setDailyPrice(i.getDailyPrice());v.setMinimumDays(i.getMinimumDays());v.setMaximumDays(i.getMaximumDays());v.setMaxGuests(i.getMaxGuests());v.setRequiresApproval(i.isRequiresApproval());v.setRequiresPayment(i.isRequiresPayment());v.setActive(i.isActive());}
    private void validate(Venue v){if(v.getName()==null||v.getName().isBlank()||v.getType()==null||v.getType().isBlank())throw new BusinessException("Nome e tipo são obrigatórios");Venue.PricingType p=v.getPricingType()==null?Venue.PricingType.FIXED_SLOT:v.getPricingType();switch(p){case HOURLY -> {price(v.getBasePrice()!=null?v.getBasePrice():v.getPrice());int min=n(v.getMinimumDurationMinutes(),60),max=n(v.getMaximumDurationMinutes(),240),step=n(v.getDurationStepMinutes(),60);if(min<1||max<min||step<1)throw new BusinessException("Duração por hora inválida");}case DAILY -> {price(v.getDailyPrice()!=null?v.getDailyPrice():v.getPrice());if(n(v.getMinimumDays(),1)<1||n(v.getMaximumDays(),365)<n(v.getMinimumDays(),1))throw new BusinessException("Limites de diária inválidos");}case PACKAGE -> {} default -> {price(v.getBasePrice()!=null?v.getBasePrice():v.getPrice());if(n(v.getSlotDurationMinutes(),n(v.getDurationMinutes(),60))<1)throw new BusinessException("Duração inválida");}}}
    private void normalize(Venue v){if(v.getPricingType()==null)v.setPricingType(Venue.PricingType.FIXED_SLOT);if(v.getBasePrice()==null)v.setBasePrice(v.getPrice()==null?BigDecimal.ZERO:v.getPrice());if(v.getSlotDurationMinutes()==null)v.setSlotDurationMinutes(v.getDurationMinutes()==null?60:v.getDurationMinutes());if(v.getPrice()==null)v.setPrice(v.getPricingType()==Venue.PricingType.DAILY&&v.getDailyPrice()!=null?v.getDailyPrice():v.getBasePrice());if(v.getDurationMinutes()==null)v.setDurationMinutes(v.getSlotDurationMinutes()==null?60:v.getSlotDurationMinutes());}
    private void price(BigDecimal p){if(p==null||p.signum()<0)throw new BusinessException("Preço inválido");}
    private int n(Integer v,int d){return v==null?d:v;}
}