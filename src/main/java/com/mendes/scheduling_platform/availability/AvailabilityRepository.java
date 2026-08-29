package com.mendes.scheduling_platform.availability; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface AvailabilityRepository extends JpaRepository<Availability,Long> { List<Availability> findAllByTenantIdAndVenueId(Long tenantId,Long venueId); Optional<Availability> findByIdAndTenantId(Long id,Long tenantId); }
