package com.mendes.scheduling_platform.availability;
import com.mendes.scheduling_platform.exception.*;
import com.mendes.scheduling_platform.venue.*;
import org.springframework.stereotype.Service;
import java.time.LocalTime;
import java.util.*;

@Service
public class AvailabilityService {
    private final AvailabilityRepository repo;
    private final VenueRepository venues;

    public AvailabilityService(AvailabilityRepository r,VenueRepository v){repo=r;venues=v;}

    public List<Availability> list(Long t,Long venue){
        venues.findByIdAndTenantId(venue,t).orElseThrow(()->new NotFoundException("Espaço não encontrado"));
        return repo.findAllByTenantIdAndVenueId(t,venue);
    }

    public Availability save(Long t,Long venue,Availability a){
        // "00:00" as the closing time means "meia-noite / fim do dia" for the purposes of this
        // single-day schedule, not "início do dia". Normalize it to 23:59:59 so it always
        // compares as later than any start time on the same day, and so any operating hours
        // (including "aberto até meia-noite") can be configured on any plan.
        if(a.getEndTime() != null && a.getEndTime().equals(LocalTime.MIDNIGHT)){
            a.setEndTime(LocalTime.of(23,59,59));
        }
        if(a.getStartTime()==null || a.getEndTime()==null || !a.getStartTime().isBefore(a.getEndTime()))
            throw new BusinessException("Intervalo inválido");
        venues.findByIdAndTenantId(venue,t).orElseThrow(()->new NotFoundException("Espaço não encontrado"));
        a.setId(null);a.setTenantId(t);a.setVenueId(venue);
        return repo.save(a);
    }

    public void delete(Long t,Long id){
        repo.delete(repo.findByIdAndTenantId(id,t).orElseThrow(()->new NotFoundException("Disponibilidade não encontrada")));
    }
}