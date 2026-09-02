package com.mendes.scheduling_platform.subscription;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity
@Table(name="subscriptions")
@Getter @Setter @NoArgsConstructor
public class Subscription {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(unique=true) private Long tenantId;
    @Column(nullable=false) private String plan;
    @Enumerated(EnumType.STRING) @Column(nullable=false)
    private Status status=Status.PAYMENT_PENDING;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextBillingDate;
    private LocalDate lastBillingDate;
    @Column(nullable=false) private String billingCycle = "MONTHLY";

    @Column(length=80) private String asaasCustomerId;
    @Column(length=80) private String asaasSubscriptionId;
    @Column(length=120) private String asaasCheckoutId;

    public enum Status {
        ACTIVE, TRIAL, PAYMENT_PENDING, PAST_DUE, CANCELLED, EXPIRED, SUSPENDED
    }
}
