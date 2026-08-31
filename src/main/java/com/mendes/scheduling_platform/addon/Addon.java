package com.mendes.scheduling_platform.addon;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name="addons") @Getter @Setter @NoArgsConstructor
public class Addon {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private Long tenantId;
    @Column(nullable=false) private Long venueId;
    @Column(nullable=false) private String name;
    private String description;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private PricingType pricingType=PricingType.FIXED;
    @Column(nullable=false) private BigDecimal price;
    @Column(nullable=false) private boolean active=true;
    public enum PricingType { FIXED, PER_HOUR, PER_UNIT }
}
