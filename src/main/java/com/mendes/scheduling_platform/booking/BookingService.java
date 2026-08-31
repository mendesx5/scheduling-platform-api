package com.mendes.scheduling_platform.booking;

import com.mendes.scheduling_platform.addon.*;
import com.mendes.scheduling_platform.availability.AvailabilityRepository;
import com.mendes.scheduling_platform.blockedperiod.BlockedPeriodRepository;
import com.mendes.scheduling_platform.bookingaddon.*;
import com.mendes.scheduling_platform.bookingpolicy.*;
import com.mendes.scheduling_platform.customer.*;
import com.mendes.scheduling_platform.exception.*;
import com.mendes.scheduling_platform.pricing.PricingService;
import com.mendes.scheduling_platform.venue.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import java.time.*;
import java.util.*;

@Service
public class BookingService {
    private final BookingRepository bookings; private final VenueRepository venues; private final CustomerRepository customers; private final AvailabilityRepository availability; private final BlockedPeriodRepository blocked; private final PricingService pricing; private final AddonRepository addons; private final BookingAddonRepository bookingAddons; private final VenueBookingPolicyRepository policies;
    public BookingService(BookingRepository b,VenueRepository v,CustomerRepository c,AvailabilityRepository a,BlockedPeriodRepository blocked,PricingService pricing,AddonRepository addons,BookingAddonRepository ba,VenueBookingPolicyRepository policies){this.bookings=b;this.venues=v;this.customers=c;this.availability=a;this.blocked=blocked;this.pricing=pricing;this.addons=addons;this.bookingAddons=ba;this.policies=policies;}
    public record Request(Long venueId,String customerName,String customerPhone,String customerEmail,OffsetDateTime startDateTime,Integer durationMinutes,Integer days,Long packageId,List<PricingService.AddonRequest> addons,String notes){}

    public PricingService.Quote quote(Long tenantId,Request r){Venue v=venue(tenantId,r.venueId());var q=pricing.quote(tenantId,v,r.startDateTime(),r.durationMinutes(),r.days(),r.packageId(),r.addons());validateSchedule(tenantId,v,q.startDateTime(),q.endDateTime());return q;}
    @Transactional(isolation=Isolation.READ_COMMITTED)
    public Booking create(Long tenantId,Request r){if(r.customerName()==null||r.customerName().isBlank()||r.customerPhone()==null||r.customerPhone().isBlank())throw new BusinessException("Nome e telefone são obrigatórios");Venue v=venue(tenantId,r.venueId());var q=pricing.quote(tenantId,v,r.startDateTime(),r.durationMinutes(),r.days(),r.packageId(),r.addons());validateSchedule(tenantId,v,q.startDateTime(),q.endDateTime());Customer c=customers.findByTenantIdAndPhone(tenantId,r.customerPhone()).orElseGet(Customer::new);c.setTenantId(tenantId);c.setName(r.customerName());c.setPhone(r.customerPhone());c.setEmail(r.customerEmail());c=customers.save(c);Booking b=new Booking();b.setTenantId(tenantId);b.setVenueId(v.getId());b.setCustomerId(c.getId());b.setStartDateTime(q.startDateTime());b.setEndDateTime(q.endDateTime());b.setBaseAmount(q.baseAmount());b.setAddonsAmount(q.addonsAmount());b.setDiscountAmount(java.math.BigDecimal.ZERO);b.setTotalAmount(q.totalAmount());b.setNotes(r.notes());try{b=bookings.saveAndFlush(b);}catch(DataIntegrityViolationException e){throw new BusinessException("Horário já reservado por outra solicitação");}saveAddons(tenantId,v,b,r.addons());return b;}
    public List<Booking> list(Long tenantId){return bookings.findAllByTenantId(tenantId);}
    public Booking status(Long tenantId,Long id,Booking.Status status,Booking.PaymentStatus paymentStatus){Booking b=bookings.findByIdAndTenantId(id,tenantId).orElseThrow(()->new NotFoundException("Reserva não encontrada"));if(status!=null){if(!allowed(b.getStatus(),status))throw new BusinessException("Transição de status inválida");b.setStatus(status);}if(paymentStatus!=null)b.setPaymentStatus(paymentStatus);return bookings.save(b);}
    private Venue venue(Long tenantId,Long id){return venues.findByIdAndTenantId(id,tenantId).filter(Venue::isActive).orElseThrow(()->new NotFoundException("Espaço não encontrado"));}
    private void validateSchedule(Long tenantId,Venue v,OffsetDateTime start,OffsetDateTime end){VenueBookingPolicy p=policies.findByTenantIdAndVenueId(tenantId,v.getId()).orElse(null);OffsetDateTime now=OffsetDateTime.now();int min=p==null?0:p.getMinimumAdvanceMinutes();int max=p==null?365:p.getMaximumAdvanceDays();if(!start.isAfter(now.plusMinutes(min)))throw new BusinessException("Reserva não respeita a antecedência mínima");if(start.isAfter(now.plusDays(max)))throw new BusinessException("Reserva além do período máximo permitido");boolean daily=v.getPricingType()==Venue.PricingType.DAILY;boolean inside=availability.findAllByTenantIdAndVenueId(tenantId,v.getId()).stream().anyMatch(a->a.getDayOfWeek()==start.getDayOfWeek()&&!start.toLocalTime().isBefore(a.getStartTime())&&(daily||(!end.toLocalTime().isAfter(a.getEndTime())&&start.toLocalDate().equals(end.toLocalDate()))));if(!inside)throw new BusinessException("Horário fora da disponibilidade");if(blocked.overlaps(tenantId,v.getId(),start,end))throw new BusinessException("Período bloqueado");if(bookings.existsConflict(tenantId,v.getId(),start,end))throw new BusinessException("Horário já reservado");}
    private void saveAddons(Long tenantId,Venue v,Booking b,List<PricingService.AddonRequest> selected){if(selected==null)return;for(var r:selected){Addon a=addons.findByIdAndTenantIdAndVenueId(r.addonId(),tenantId,v.getId()).filter(Addon::isActive).orElseThrow(()->new NotFoundException("Adicional não encontrado"));int qty=Math.max(1,r.quantity()==null?1:r.quantity());BookingAddon ba=new BookingAddon();ba.setTenantId(tenantId);ba.setBookingId(b.getId());ba.setAddonId(a.getId());ba.setAddonName(a.getName());ba.setQuantity(qty);ba.setUnitPrice(a.getPrice());ba.setTotalPrice(pricing.addonTotal(a,qty,b.getStartDateTime(),b.getEndDateTime()));bookingAddons.save(ba);}}
    private boolean allowed(Booking.Status from,Booking.Status to){if(from==to)return true;return switch(from){case PENDING -> to==Booking.Status.CONFIRMED||to==Booking.Status.CANCELLED;case CONFIRMED -> to==Booking.Status.COMPLETED||to==Booking.Status.CANCELLED;case CANCELLED,COMPLETED -> false;};}
}
