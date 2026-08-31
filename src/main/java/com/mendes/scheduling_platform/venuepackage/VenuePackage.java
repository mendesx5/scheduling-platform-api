package com.mendes.scheduling_platform.venuepackage;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name="venue_packages") @Getter @Setter @NoArgsConstructor
public class VenuePackage {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private Long tenantId;
    @Column(nullable=false) private Long venueId;
    @Column(nullable=false) private String name;
    private String description;
    @Column(nullable=false) private Integer durationMinutes;
    @Column(nullable=false) private BigDecimal price;
    @Column(nullable=false) private boolean active = true;
}
