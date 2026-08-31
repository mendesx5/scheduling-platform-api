package com.mendes.scheduling_platform.venuepackage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface VenuePackageRepository extends JpaRepository<VenuePackage,Long> {
    List<VenuePackage> findAllByTenantIdAndVenueId(Long tenantId,Long venueId);
    List<VenuePackage> findAllByTenantIdAndVenueIdAndActiveTrue(Long tenantId,Long venueId);
    Optional<VenuePackage> findByIdAndTenantIdAndVenueId(Long id,Long tenantId,Long venueId);
}
