package com.mendes.scheduling_platform.platform;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog,Long>{List<AdminAuditLog> findAllByTenantIdOrderByCreatedAtDesc(Long tenantId);}
