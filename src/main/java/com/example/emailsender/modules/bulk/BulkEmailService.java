package com.example.emailsender.modules.bulk;

import com.example.emailsender.modules.bulk.parser.ExcelParser;
import com.example.emailsender.modules.single.EmailService;
import com.example.emailsender.utils.validation.EmailValidator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class BulkEmailService {
    private final EmailService emailService;
    private final ExcelParser excelParser;

    public BulkEmailService(EmailService emailService, ExcelParser excelParser) {
        this.emailService = emailService;
        this.excelParser = excelParser;
    }

    public int sendBulk(MultipartFile file, String subject, String body) {
        List<String> emails = excelParser.parseEmails(file);
        int sent = 0;
        for (String to : emails) {
            if (EmailValidator.isValid(to)) {
                emailService.sendSimpleEmail(to, subject, body);
                sent++;
            }
        }
        return sent;
    }
}
