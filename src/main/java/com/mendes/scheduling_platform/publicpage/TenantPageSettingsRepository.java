package com.mendes.scheduling_platform.publicpage;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface TenantPageSettingsRepository extends JpaRepository<TenantPageSettings,Long> { Optional<TenantPageSettings> findByTenantId(Long tenantId); }
