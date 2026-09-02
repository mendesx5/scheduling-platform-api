package com.mendes.scheduling_platform.password;
import org.springframework.data.jpa.repository.JpaRepository; import java.time.Instant; import java.util.*;
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken,Long>{Optional<PasswordResetToken> findByTokenHash(String hash);void deleteAllByExpiresAtBefore(Instant now);}
