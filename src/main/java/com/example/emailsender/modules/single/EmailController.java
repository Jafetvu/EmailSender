package com.example.emailsender.modules.single;

import com.example.emailsender.modules.single.dto.EmailRequest;
import jakarta.validation.Valid;
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

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendOne(@Valid @RequestBody EmailRequest req) {
        emailService.sendSimpleEmail(req.getTo(), req.getSubject(), req.getBody());
        return ResponseEntity.ok("Correo enviado a " + req.getTo());
    }

    @PostMapping(
            path = "/send/inline",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> sendInline(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam("body") String bodyHtml,
            @RequestParam(value="images", required=false) MultipartFile[] images
    ) {
        List<MultipartFile> imgs = images != null ? List.of(images) : List.of();
        emailService.sendEmailWithInlineImages(to, subject, bodyHtml, imgs);
        return ResponseEntity.ok("Enviado con imágenes inline.");
    }


    @PostMapping(
            path = "/send/advanced",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> sendAdvanced(
            @RequestParam("to") String to,
            @RequestParam("subject") String subject,
            @RequestParam("body") String bodyHtml,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            @RequestParam(value = "attachments", required = false) MultipartFile[] attachments
    ) {
        List<MultipartFile> inlineImages = images != null
                ? List.of(images)
                : List.of();

        List<MultipartFile> attachFiles = attachments != null
                ? List.of(attachments)
                : List.of();

        emailService.sendEmailWithAttachmentsAndInline(
                to, subject, bodyHtml, inlineImages, attachFiles
        );
        return ResponseEntity.ok("Correo enviado a " + to + " con inline y adjuntos");
    }


}