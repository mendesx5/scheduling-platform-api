package com.mendes.scheduling_platform.venue;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name="venues")
@Getter @Setter @NoArgsConstructor
public class Venue {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private Long tenantId;
    @Column(nullable=false) private String name;
    private String description;
    @Column(nullable=false) private String type;

    /** Legacy fields kept for backwards compatibility with existing clients. */
    @Column(nullable=false) private BigDecimal price = BigDecimal.ZERO;
    @Column(nullable=false) private Integer durationMinutes = 60;

    @Enumerated(EnumType.STRING) @Column(nullable=false) private PricingType pricingType = PricingType.FIXED_SLOT;
    private BigDecimal basePrice;
    private Integer slotDurationMinutes;
    private Integer minimumDurationMinutes;
    private Integer maximumDurationMinutes;
    private Integer durationStepMinutes;
    private BigDecimal dailyPrice;
    private Integer minimumDays;
    private Integer maximumDays;
    private Integer maxGuests;
    @Column(nullable=false) private boolean requiresApproval = true;
    @Column(nullable=false) private boolean requiresPayment = false;
    @Column(nullable=false) private boolean active = true;

    public enum PricingType { FIXED_SLOT, HOURLY, DAILY, PACKAGE }
}
