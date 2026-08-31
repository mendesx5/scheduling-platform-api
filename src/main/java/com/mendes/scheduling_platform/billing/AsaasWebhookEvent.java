package com.mendes.scheduling_platform.billing;
import jakarta.persistence.*; import lombok.*; import java.time.Instant;
@Entity @Table(name="asaas_webhook_events") @Getter @Setter @NoArgsConstructor
public class AsaasWebhookEvent {
 @Id @Column(length=100) private String id;
 @Column(nullable=false,length=80) private String event;
 @Column(nullable=false) private Instant receivedAt=Instant.now();
}
