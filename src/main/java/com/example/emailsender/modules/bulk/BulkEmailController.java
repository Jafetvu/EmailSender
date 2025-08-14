package com.example.emailsender.modules.bulk;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/email")
public class BulkEmailController {

    private final BulkEmailService bulkEmailService;

    public BulkEmailController(BulkEmailService bulkEmailService) {
        this.bulkEmailService = bulkEmailService;
    }

    // Envío masivo de texto plano
    @PostMapping(
            path = "/send/bulk",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> sendBulk(
            @RequestParam("file") MultipartFile file,
            @RequestParam("subject") String subject,
            @RequestParam("body") String body,
            @RequestParam(value = "footer", required = false) String footer  // Nuevo parámetro opcional
    ) {
        int count = bulkEmailService.sendBulk(file, subject, body, footer != null ? footer : "");
        return ResponseEntity.ok("Enviados a " + count + " destinatarios");
    }

    // Envío masivo con imágenes inline (HTML)
    @PostMapping(
            path = "/send/bulk-inline",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> sendBulkInline(
            @RequestParam("file") MultipartFile excel,
            @RequestParam("subject") String subject,
            @RequestParam("body") String bodyHtml,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            @RequestParam(value = "footer", required = false) String footer  // Nuevo parámetro opcional
    ) {
        List<MultipartFile> imgs = images != null ? Arrays.asList(images) : List.of();
        int count = bulkEmailService.sendBulkInline(excel, subject, bodyHtml, imgs, footer != null ? footer : "");
        return ResponseEntity.ok("Enviados a " + count + " destinatarios con imágenes inline");
    }

    // Envío masivo con imágenes inline + adjuntos
    @PostMapping(
            path = "/send/bulk-advanced",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> sendBulkAdvanced(
            @RequestParam("file") MultipartFile excel,
            @RequestParam("subject") String subject,
            @RequestParam("body") String bodyHtml,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            @RequestParam(value = "attachments", required = false) MultipartFile[] attachments,
            @RequestParam(value = "footer", required = false) String footer  // Nuevo parámetro opcional
    ) {
        List<MultipartFile> inlineImgs = images != null ? Arrays.asList(images) : List.of();
        List<MultipartFile> attachFiles = attachments != null ? Arrays.asList(attachments) : List.of();
        int sent = bulkEmailService.sendBulkWithAttachmentsAndInline(
                excel, subject, bodyHtml, inlineImgs, attachFiles,
                footer != null ? footer : ""
        );
        return ResponseEntity.ok("Enviados a " + sent + " destinatarios");
    }
}