package com.mendes.scheduling_platform.platform; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface PlatformAdminRepository extends JpaRepository<PlatformAdmin,Long> { Optional<PlatformAdmin> findByEmailIgnoreCase(String email); }
