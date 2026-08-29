package com.mendes.scheduling_platform.customer; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface CustomerRepository extends JpaRepository<Customer,Long> { List<Customer> findAllByTenantId(Long tenantId); Optional<Customer> findByIdAndTenantId(Long id,Long tenantId); Optional<Customer> findByTenantIdAndPhone(Long tenantId,String phone); }
