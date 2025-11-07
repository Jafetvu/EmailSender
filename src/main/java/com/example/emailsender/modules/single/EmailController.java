package com.example.emailsender.modules.single;

import com.example.emailsender.modules.single.dto.SafeFileDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendSingleEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String body,
            @RequestParam(required = false) String footer) {

        emailService.sendSimpleEmail(to, subject, body, footer != null ? footer : "");
        return ResponseEntity.ok("Correo enviado exitosamente");
    }

    @PostMapping("/send/inline")
    public ResponseEntity<String> sendEmailWithImages(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String htmlBody,
            @RequestPart(required = false) List<MultipartFile> inlineImages,
            @RequestParam(required = false) String footer) {

        List<SafeFileDto> safeInline = convertToSafeFiles(inlineImages);
        emailService.sendEmailWithInlineImages(to, subject, htmlBody, safeInline, footer != null ? footer : "");
        return ResponseEntity.ok("Correo con imágenes enviado");
    }

    @PostMapping("/send/advanced")
    public ResponseEntity<String> sendFullEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String htmlBody,
            @RequestPart(required = false) List<MultipartFile> inlineImages,
            @RequestPart(required = false) List<MultipartFile> attachments,
            @RequestParam(required = false) String footer) {

        List<SafeFileDto> safeInline = convertToSafeFiles(inlineImages);
        List<SafeFileDto> safeAttachments = convertToSafeFiles(attachments);

        emailService.sendEmailWithAttachmentsAndInline(
                to,
                subject,
                htmlBody,
                safeInline,
                safeAttachments,
                footer != null ? footer : ""
        );
        return ResponseEntity.ok("Correo completo enviado");
    }

    private List<SafeFileDto> convertToSafeFiles(List<MultipartFile> files) {
        if (files == null) return null;
        List<SafeFileDto> safe = new java.util.ArrayList<>();
        for (MultipartFile f : files) {
            try {
                if (f.getOriginalFilename() == null) continue;
                safe.add(new SafeFileDto(f.getOriginalFilename(), f.getContentType(), f.getBytes()));
            } catch (Exception e) {
                throw new RuntimeException("Error leyendo archivo adjunto", e);
            }
        }
        return safe;
    }
}
