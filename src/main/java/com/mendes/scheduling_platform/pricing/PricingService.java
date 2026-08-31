package com.mendes.scheduling_platform.pricing;

import com.mendes.scheduling_platform.addon.*;
import com.mendes.scheduling_platform.exception.*;
import com.mendes.scheduling_platform.venue.*;
import com.mendes.scheduling_platform.venuepackage.*;
import org.springframework.stereotype.Service;
import java.math.*;
import java.time.*;
import java.util.*;

@Service
public class PricingService {
    public record AddonRequest(Long addonId,Integer quantity){}
    public record Quote(BigDecimal baseAmount,BigDecimal addonsAmount,BigDecimal totalAmount,OffsetDateTime startDateTime,OffsetDateTime endDateTime){}
    private final VenuePackageRepository packages; private final AddonRepository addons;
    public PricingService(VenuePackageRepository p,AddonRepository a){packages=p;addons=a;}

    public Quote quote(Long tenantId, Venue venue, OffsetDateTime start, Integer durationMinutes, Integer days, Long packageId, List<AddonRequest> selected){
        if(start==null)throw new BusinessException("Data/hora inicial obrigatória");
        Venue.PricingType type=venue.getPricingType()==null?Venue.PricingType.FIXED_SLOT:venue.getPricingType();
        BigDecimal base; OffsetDateTime end;
        switch(type){
            case HOURLY -> {int duration=durationMinutes==null?value(venue.getMinimumDurationMinutes(),60):durationMinutes;validateHourly(venue,duration);BigDecimal hourly=nonNegative(first(venue.getBasePrice(),venue.getPrice()),"Preço por hora inválido");base=hourly.multiply(BigDecimal.valueOf(duration)).divide(BigDecimal.valueOf(60),2,RoundingMode.HALF_UP);end=start.plusMinutes(duration);}
            case DAILY -> {int d=days==null?value(venue.getMinimumDays(),1):days;int min=value(venue.getMinimumDays(),1),max=value(venue.getMaximumDays(),365);if(d<min||d>max)throw new BusinessException("Quantidade de diárias fora do permitido");base=nonNegative(first(venue.getDailyPrice(),venue.getPrice()),"Valor da diária inválido").multiply(BigDecimal.valueOf(d));end=start.plusDays(d);}
            case PACKAGE -> {if(packageId==null)throw new BusinessException("Selecione um pacote");VenuePackage p=packages.findByIdAndTenantIdAndVenueId(packageId,tenantId,venue.getId()).filter(VenuePackage::isActive).orElseThrow(()->new NotFoundException("Pacote não encontrado"));base=p.getPrice();end=start.plusMinutes(p.getDurationMinutes());}
            default -> {int duration=value(venue.getSlotDurationMinutes(),value(venue.getDurationMinutes(),60));base=nonNegative(first(venue.getBasePrice(),venue.getPrice()),"Preço inválido");end=start.plusMinutes(duration);}
        }
        BigDecimal extras=BigDecimal.ZERO;
        if(selected!=null)for(AddonRequest req:selected){if(req==null||req.addonId()==null)continue;Addon a=addons.findByIdAndTenantIdAndVenueId(req.addonId(),tenantId,venue.getId()).filter(Addon::isActive).orElseThrow(()->new NotFoundException("Adicional não encontrado"));int qty=Math.max(1,req.quantity()==null?1:req.quantity());BigDecimal amount=switch(a.getPricingType()){case PER_UNIT -> a.getPrice().multiply(BigDecimal.valueOf(qty));case PER_HOUR -> a.getPrice().multiply(BigDecimal.valueOf(Duration.between(start,end).toMinutes())).divide(BigDecimal.valueOf(60),2,RoundingMode.HALF_UP);default -> a.getPrice();};extras=extras.add(amount);}
        return new Quote(base.setScale(2,RoundingMode.HALF_UP),extras.setScale(2,RoundingMode.HALF_UP),base.add(extras).setScale(2,RoundingMode.HALF_UP),start,end);
    }
    public BigDecimal addonTotal(Addon addon,int quantity,OffsetDateTime start,OffsetDateTime end){return switch(addon.getPricingType()){case PER_UNIT -> addon.getPrice().multiply(BigDecimal.valueOf(Math.max(1,quantity)));case PER_HOUR -> addon.getPrice().multiply(BigDecimal.valueOf(Duration.between(start,end).toMinutes())).divide(BigDecimal.valueOf(60),2,RoundingMode.HALF_UP);default -> addon.getPrice();};}
    private void validateHourly(Venue v,int d){int min=value(v.getMinimumDurationMinutes(),60),max=value(v.getMaximumDurationMinutes(),1440),step=value(v.getDurationStepMinutes(),60);if(d<min||d>max||(d-min)%step!=0)throw new BusinessException("Duração fora das regras do espaço");}
    private int value(Integer v,int fallback){return v==null?fallback:v;}
    private BigDecimal first(BigDecimal a,BigDecimal b){return a!=null?a:b;}
    private BigDecimal nonNegative(BigDecimal n,String msg){if(n==null||n.signum()<0)throw new BusinessException(msg);return n;}
}
