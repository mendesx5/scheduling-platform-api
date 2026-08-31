package com.mendes.scheduling_platform.bookingaddon;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name="booking_addons") @Getter @Setter @NoArgsConstructor
public class BookingAddon {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private Long tenantId;
    @Column(nullable=false) private Long bookingId;
    @Column(nullable=false) private Long addonId;
    @Column(nullable=false) private String addonName;
    @Column(nullable=false) private Integer quantity;
    @Column(nullable=false) private BigDecimal unitPrice;
    @Column(nullable=false) private BigDecimal totalPrice;
}
