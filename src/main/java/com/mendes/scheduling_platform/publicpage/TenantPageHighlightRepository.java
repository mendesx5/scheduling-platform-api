package com.mendes.scheduling_platform.publicpage;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface TenantPageHighlightRepository extends JpaRepository<TenantPageHighlight,Long> { List<TenantPageHighlight> findAllByTenantIdOrderBySortOrderAsc(Long tenantId); void deleteByTenantId(Long tenantId); }
