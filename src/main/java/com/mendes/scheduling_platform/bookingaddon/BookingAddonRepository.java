package com.mendes.scheduling_platform.bookingaddon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface BookingAddonRepository extends JpaRepository<BookingAddon,Long> { List<BookingAddon> findAllByTenantIdAndBookingId(Long tenantId,Long bookingId); }
