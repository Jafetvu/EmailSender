package com.example.emailsender.modules.single;

import com.example.emailsender.modules.single.dto.EmailRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.emailsender.modules.single.EmailService;
@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendOne(@Valid @RequestBody EmailRequest req) {
        emailService.sendSimpleEmail(req.getTo(), req.getSubject(), req.getBody());
        return ResponseEntity.ok("Correo enviado a " + req.getTo());
    }
}