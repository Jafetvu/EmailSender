package com.example.emailsender.modules.tracking;

import com.example.emailsender.modules.tracking.entity.EmailTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmailTrackingRepository extends JpaRepository<EmailTracking, Long> {
    Optional<EmailTracking> findByToken(String token);
}
