package com.mendes.scheduling_platform.addon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface AddonRepository extends JpaRepository<Addon,Long> {
    List<Addon> findAllByTenantIdAndVenueId(Long tenantId,Long venueId);
    List<Addon> findAllByTenantIdAndVenueIdAndActiveTrue(Long tenantId,Long venueId);
    Optional<Addon> findByIdAndTenantIdAndVenueId(Long id,Long tenantId,Long venueId);
    long countByTenantIdAndVenueId(Long tenantId,Long venueId);
}