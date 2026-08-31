package com.mendes.scheduling_platform.subscription;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface SubscriptionRepository extends JpaRepository<Subscription,Long> {
 Optional<Subscription> findByTenantId(Long tenantId);
 Optional<Subscription> findByAsaasCheckoutId(String asaasCheckoutId);
 Optional<Subscription> findByAsaasSubscriptionId(String asaasSubscriptionId);
}
