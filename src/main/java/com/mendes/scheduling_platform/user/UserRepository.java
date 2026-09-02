package com.mendes.scheduling_platform.user;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface UserRepository extends JpaRepository<User,Long> {
 Optional<User> findByTenantIdAndEmailIgnoreCase(Long tenantId,String email);
 Optional<User> findFirstByEmailIgnoreCase(String email);
 Optional<User> findByIdAndTenantId(Long id,Long tenantId);
 List<User> findAllByTenantId(Long tenantId);
 boolean existsByTenantIdAndEmailIgnoreCase(Long tenantId,String email);
 boolean existsByTenantIdAndUsernameIgnoreCase(Long tenantId,String username);
 long countByTenantId(Long tenantId);
 long countByTenantIdAndActiveTrue(Long tenantId);
}
