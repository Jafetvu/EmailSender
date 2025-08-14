package com.example.emailsender.modules.single;

import com.example.emailsender.modules.single.dto.EmailRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.emailsender.modules.single.EmailService;

import java.util.List;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    // Correo simple con parámetro footer opcional
    @PostMapping("/send")
    public ResponseEntity<String> sendSingleEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String body,
            @RequestParam(required = false) String footer) {

        emailService.sendSimpleEmail(to, subject, body, footer != null ? footer : "");
        return ResponseEntity.ok("Correo enviado exitosamente");
    }

    // Correo con imágenes + footer opcional
    @PostMapping("/send/inline")
    public ResponseEntity<String> sendEmailWithImages(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String htmlBody,
            @RequestPart(required = false) List<MultipartFile> inlineImages,
            @RequestParam(required = false) String footer) {

        emailService.sendEmailWithInlineImages(to, subject, htmlBody, inlineImages, footer != null ? footer : "");
        return ResponseEntity.ok("Correo con imágenes enviado");
    }

    // Correo completo + footer opcional
    @PostMapping("/send/advanced")
    public ResponseEntity<String> sendFullEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String htmlBody,
            @RequestPart(required = false) List<MultipartFile> inlineImages,
            @RequestPart(required = false) List<MultipartFile> attachments,
            @RequestParam(required = false) String footer) {

        emailService.sendEmailWithAttachmentsAndInline(
                to,
                subject,
                htmlBody,
                inlineImages,
                attachments,
                footer != null ? footer : ""
        );
        return ResponseEntity.ok("Correo completo enviado");
    }
}