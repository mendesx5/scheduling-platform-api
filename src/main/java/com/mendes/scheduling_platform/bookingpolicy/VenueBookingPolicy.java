package com.mendes.scheduling_platform.bookingpolicy;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="venue_booking_policies") @Getter @Setter @NoArgsConstructor
public class VenueBookingPolicy {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private Long tenantId;
    @Column(nullable=false,unique=true) private Long venueId;
    @Column(nullable=false) private boolean requiresApproval=true;
    @Column(nullable=false) private Integer minimumAdvanceMinutes=0;
    @Column(nullable=false) private Integer maximumAdvanceDays=365;
    @Column(nullable=false) private boolean cancellationAllowed=true;
    @Column(nullable=false) private Integer cancellationDeadlineHours=24;
}
