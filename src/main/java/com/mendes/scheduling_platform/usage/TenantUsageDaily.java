package com.mendes.scheduling_platform.usage;
import jakarta.persistence.*; import lombok.*; import java.math.BigDecimal; import java.time.LocalDate;
@Entity @Table(name="tenant_usage_daily",uniqueConstraints=@UniqueConstraint(columnNames={"tenant_id","usage_date"}))
@Getter @Setter @NoArgsConstructor
public class TenantUsageDaily { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; @Column(nullable=false) Long tenantId; @Column(nullable=false) LocalDate usageDate; long apiRequests; long bookingsCreated; long bookingsCancelled; long activeUsers; long customersCreated; @Column(nullable=false) BigDecimal revenue=BigDecimal.ZERO; }
