package com.mendes.scheduling_platform.tenant;
import jakarta.persistence.*; import lombok.*; import java.time.Instant;
@Entity @Table(name="tenants") @Getter @Setter @NoArgsConstructor
public class Tenant {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String name;
    @Column(nullable=false,unique=true) private String slug;
    private String logoUrl; private String coverUrl; private String primaryColor; private String phone; private String instagram; private String address;
    @Enumerated(EnumType.STRING) private TenantStatus status=TenantStatus.ACTIVE;
    private String plan="STARTER";
    @Column(nullable=false) private String timezone="America/Sao_Paulo";
    @Column(nullable=false) private String dateFormat="dd/MM/yyyy";
    @Column(nullable=false) private String timeFormat="24h";
    @Column(nullable=false) private String weekStartsOn="MONDAY";
    @Column(nullable=false) private boolean notifyNewBooking=true;
    @Column(nullable=false) private boolean notifyCancellation=true;
    @Column(nullable=false) private boolean notifyBookingReminder=true;
    @Column(nullable=false) private boolean notifyEmail=true;
    @Column(nullable=false) private boolean notifyWhatsapp=false;
    private Instant createdAt=Instant.now();
    public enum TenantStatus { ACTIVE, PENDING_PAYMENT, SUSPENDED }
}
