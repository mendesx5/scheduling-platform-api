package com.mendes.scheduling_platform.bookingpolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface VenueBookingPolicyRepository extends JpaRepository<VenueBookingPolicy,Long> { Optional<VenueBookingPolicy> findByTenantIdAndVenueId(Long tenantId,Long venueId); }
