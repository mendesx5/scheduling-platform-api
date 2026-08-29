package com.mendes.scheduling_platform.booking;

import com.mendes.scheduling_platform.availability.AvailabilityRepository;
import com.mendes.scheduling_platform.blockedperiod.BlockedPeriodRepository;
import com.mendes.scheduling_platform.customer.Customer;
import com.mendes.scheduling_platform.customer.CustomerRepository;
import com.mendes.scheduling_platform.exception.BusinessException;
import com.mendes.scheduling_platform.exception.NotFoundException;
import com.mendes.scheduling_platform.venue.Venue;
import com.mendes.scheduling_platform.venue.VenueRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class BookingService {
    private final BookingRepository bookings;
    private final VenueRepository venues;
    private final CustomerRepository customers;
    private final AvailabilityRepository availability;
    private final BlockedPeriodRepository blocked;

    public BookingService(BookingRepository bookings, VenueRepository venues, CustomerRepository customers,
                          AvailabilityRepository availability, BlockedPeriodRepository blocked) {
        this.bookings = bookings;
        this.venues = venues;
        this.customers = customers;
        this.availability = availability;
        this.blocked = blocked;
    }

    public record Request(Long venueId, String customerName, String customerPhone,
                          String customerEmail, OffsetDateTime startDateTime) {}

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Booking create(Long tenantId, Request request) {
        Venue venue = venues.findByIdAndTenantId(request.venueId(), tenantId)
                .filter(Venue::isActive)
                .orElseThrow(() -> new NotFoundException("Espaço não encontrado"));
        OffsetDateTime start = request.startDateTime();
        OffsetDateTime end = start.plusMinutes(venue.getDurationMinutes());

        if (!start.isAfter(OffsetDateTime.now())) {
            throw new BusinessException("A reserva deve estar no futuro");
        }
        boolean insideAvailability = availability.findAllByTenantIdAndVenueId(tenantId, venue.getId()).stream()
                .anyMatch(item -> item.getDayOfWeek() == start.getDayOfWeek()
                        && !start.toLocalTime().isBefore(item.getStartTime())
                        && !end.toLocalTime().isAfter(item.getEndTime())
                        && start.toLocalDate().equals(end.toLocalDate()));
        if (!insideAvailability) throw new BusinessException("Horário fora da disponibilidade");
        if (blocked.overlaps(tenantId, venue.getId(), start, end)) {
            throw new BusinessException("Período bloqueado");
        }
        // Fast feedback; the PostgreSQL exclusion constraint remains the concurrency authority.
        if (bookings.existsConflict(tenantId, venue.getId(), start, end)) {
            throw new BusinessException("Horário já reservado");
        }

        Customer customer = customers.findByTenantIdAndPhone(tenantId, request.customerPhone())
                .orElseGet(Customer::new);
        customer.setTenantId(tenantId);
        customer.setName(request.customerName());
        customer.setPhone(request.customerPhone());
        customer.setEmail(request.customerEmail());
        customer = customers.save(customer);

        Booking booking = new Booking();
        booking.setTenantId(tenantId);
        booking.setVenueId(venue.getId());
        booking.setCustomerId(customer.getId());
        booking.setStartDateTime(start);
        booking.setEndDateTime(end);
        booking.setTotalAmount(venue.getPrice());
        try {
            return bookings.saveAndFlush(booking);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException("Horário já reservado por outra solicitação");
        }
    }

    public List<Booking> list(Long tenantId) {
        return bookings.findAllByTenantId(tenantId);
    }

    public Booking status(Long tenantId, Long id, Booking.Status status, Booking.PaymentStatus paymentStatus) {
        Booking booking = bookings.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada"));
        if (status != null) booking.setStatus(status);
        if (paymentStatus != null) booking.setPaymentStatus(paymentStatus);
        return bookings.save(booking);
    }
}
