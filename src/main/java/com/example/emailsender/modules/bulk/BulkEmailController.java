package com.example.emailsender.modules.bulk;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/email")
public class BulkEmailController {
    private final BulkEmailService bulkEmailService;

    public BulkEmailController(BulkEmailService bulkEmailService) {
        this.bulkEmailService = bulkEmailService;
    }

    @PostMapping(
            path = "/send/bulk",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> sendBulk(
            @RequestParam("file") MultipartFile file,
            @RequestParam("subject") String subject,
            @RequestParam("body") String body) {

        int count = bulkEmailService.sendBulk(file, subject, body);
        return ResponseEntity.ok("Enviados a " + count + " destinatarios");
    }
}

