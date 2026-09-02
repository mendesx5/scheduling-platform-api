package com.mendes.scheduling_platform.platform;
import jakarta.persistence.*; import lombok.*; import java.time.Instant;
@Entity @Table(name="admin_audit_logs") @Getter @Setter @NoArgsConstructor
public class AdminAuditLog { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; Long adminId; Long tenantId; @Column(nullable=false) String action; @Column(columnDefinition="text") String details; Instant createdAt=Instant.now(); }
