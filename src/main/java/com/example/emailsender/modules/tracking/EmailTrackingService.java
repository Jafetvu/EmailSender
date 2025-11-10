package com.example.emailsender.modules.tracking;

import com.example.emailsender.modules.tracking.entity.EmailTracking;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmailTrackingService {

    private final EmailTrackingRepository repository;

    public EmailTrackingService(EmailTrackingRepository repository) {
        this.repository = repository;
    }

    public String generateTrackingPixel(String recipient) {
        String token = UUID.randomUUID().toString();
        EmailTracking tracking = new EmailTracking(token, recipient);
        repository.save(tracking);
        return "<img src=\"/api/pixel/" + token
                + "\" alt=\"\" style=\"width:1px;height:1px;display:none;\" />";
    }

    public void registerOpen(String token) {
        repository.findByToken(token).ifPresent(tracking -> {
            tracking.setOpenCount(tracking.getOpenCount() + 1); // incrementa el contador
            if (tracking.getOpenedAt() == null) {
                tracking.setOpenedAt(LocalDateTime.now());
            }
            repository.save(tracking);
        });
    }

    public int getOpenCount(String token) {
        return repository.findByToken(token).map(EmailTracking::getOpenCount).orElse(0);
    }
}
