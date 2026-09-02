package com.mendes.scheduling_platform.usage;
import org.springframework.data.jpa.repository.JpaRepository; import java.time.LocalDate; import java.util.*;
public interface TenantUsageDailyRepository extends JpaRepository<TenantUsageDaily,Long>{Optional<TenantUsageDaily> findByTenantIdAndUsageDate(Long tenantId,LocalDate date);List<TenantUsageDaily> findAllByTenantIdAndUsageDateBetweenOrderByUsageDate(Long tenantId,LocalDate start,LocalDate end);}
