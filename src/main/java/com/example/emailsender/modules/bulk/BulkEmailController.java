package com.example.emailsender.modules.bulk;

import com.example.emailsender.modules.single.dto.SafeFileDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Controlador REST para operaciones de envío de correos masivos.
 */
@RestController
@RequestMapping("/api/email/bulk")
public class BulkEmailController {

    private final BulkEmailService bulkEmailService;

    public BulkEmailController(BulkEmailService bulkEmailService) {
        this.bulkEmailService = bulkEmailService;
    }

    /**
     * Envía correos de texto plano a una lista de destinatarios extraída del Excel.
     */
    @PostMapping("/send")
    public CompletableFuture<ResponseEntity<String>> sendBulkEmails(
            @RequestParam("file") MultipartFile file,
            @RequestParam("subject") String subject,
            @RequestParam("body") String body,
            @RequestParam(value = "footer", required = false) String footer) {

        return bulkEmailService.sendBulk(file, subject, body, footer)
                .thenApply(count -> ResponseEntity.ok("Enviados " + count + " correos exitosamente"))
                .exceptionally(ex -> ResponseEntity.status(500).body("Error: " + ex.getMessage()));
    }

    /**
     * Envía correos HTML con imágenes inline a una lista de destinatarios.
     */
    @PostMapping("/send-inline")
    public CompletableFuture<ResponseEntity<String>> sendBulkInline(
            @RequestParam("excel") MultipartFile excel,
            @RequestParam("subject") String subject,
            @RequestParam("bodyHtml") String bodyHtml,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "footer", required = false) String footer) {

        List<SafeFileDto> safeImages = convertToSafeFiles(images);

        return bulkEmailService.sendBulkInline(excel, subject, bodyHtml, safeImages, footer)
                .thenApply(count -> ResponseEntity.ok("Enviados " + count + " correos con imágenes exitosamente"))
                .exceptionally(ex -> ResponseEntity.status(500).body("Error: " + ex.getMessage()));
    }

    /**
     * Envía correos HTML con imágenes inline y adjuntos.
     */
    @PostMapping("/send-with-attachments")
    public CompletableFuture<ResponseEntity<String>> sendBulkWithAttachments(
            @RequestParam("excel") MultipartFile excel,
            @RequestParam("subject") String subject,
            @RequestParam("bodyHtml") String bodyHtml,
            @RequestParam(value = "inlineImages", required = false) List<MultipartFile> inlineImages,
            @RequestParam(value = "attachments", required = false) List<MultipartFile> attachments,
            @RequestParam(value = "footer", required = false) String footer) {

        List<SafeFileDto> safeInline = convertToSafeFiles(inlineImages);
        List<SafeFileDto> safeAttachments = convertToSafeFiles(attachments);

        return bulkEmailService.sendBulkWithAttachmentsAndInline(excel, subject, bodyHtml, safeInline, safeAttachments, footer)
                .thenApply(count -> ResponseEntity.ok("Enviados " + count + " correos con adjuntos exitosamente"))
                .exceptionally(ex -> ResponseEntity.status(500).body("Error: " + ex.getMessage()));
    }

    // Utilidad para convertir MultipartFile -> SafeFileDto
    private List<SafeFileDto> convertToSafeFiles(List<MultipartFile> files) {
        if (files == null) return null;
        List<SafeFileDto> safeFiles = new java.util.ArrayList<>();
        for (MultipartFile f : files) {
            try {
                if (f.getOriginalFilename() == null) continue;
                safeFiles.add(new SafeFileDto(
                        f.getOriginalFilename(),
                        f.getContentType(),
                        f.getBytes()
                ));
            } catch (Exception e) {
                throw new RuntimeException("Error leyendo archivo adjunto", e);
            }
        }
        return safeFiles;
    }
}
