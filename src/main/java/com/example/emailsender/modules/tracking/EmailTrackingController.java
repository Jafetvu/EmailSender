package com.example.emailsender.modules.tracking;

import com.example.emailsender.modules.tracking.entity.EmailTracking;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tracking")
public class EmailTrackingController {

    private final EmailTrackingRepository repository;
    private final EmailTrackingService service;

    public EmailTrackingController(EmailTrackingRepository repository,
                                   EmailTrackingService service) {
        this.repository = repository;
        this.service = service;
    }

    /**
     * Devuelve el conteo de aperturas para un token concreto.
     */
    @GetMapping("/{token}/count")
    public ResponseEntity<Integer> getOpenCount(@PathVariable String token) {
        int count = service.getOpenCount(token);
        return ResponseEntity.ok(count);
    }

    /**
     * Lista todos los registros de seguimiento con su contador.
     */
    @GetMapping("/all")
    public ResponseEntity<List<EmailTracking>> listAll() {
        return ResponseEntity.ok(repository.findAll());
    }
}
