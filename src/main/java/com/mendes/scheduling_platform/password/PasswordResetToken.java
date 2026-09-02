package com.mendes.scheduling_platform.password;
import jakarta.persistence.*; import lombok.*; import java.time.Instant;
@Entity @Table(name="password_reset_tokens") @Getter @Setter @NoArgsConstructor
public class PasswordResetToken { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; Long userId; Long platformAdminId; @Column(nullable=false,unique=true) String tokenHash; @Column(nullable=false) Instant expiresAt; Instant usedAt; Instant createdAt=Instant.now(); }
