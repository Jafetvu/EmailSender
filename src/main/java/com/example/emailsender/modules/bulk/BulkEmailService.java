package com.example.emailsender.modules.bulk;

import com.example.emailsender.modules.bulk.parser.ExcelParser;
import com.example.emailsender.modules.single.EmailService;
import com.example.emailsender.utils.validation.EmailValidator;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class BulkEmailService {

    private final JavaMailSender mailSender;
    private final ExcelParser excelParser;

    // Leyenda fija implícita
    private static final String NO_REPLY_NOTICE = "Este correo es para difusión. Por favor no responder.";

    private static final String BULK_FROM = "difusion@jorgeslubricantes.com.mx";
    private static final String BULK_REPLY_TO = "noreply@jorgeslubricantes.com.mx";

    public BulkEmailService(JavaMailSender mailSender, ExcelParser excelParser) {
        this.mailSender = mailSender;
        this.excelParser = excelParser;
    }

    @Async
    public int sendBulk(
            MultipartFile file,
            String subject,
            String body,
            String footer  // Nuevo parámetro footer
    ) {
        List<String> emails = excelParser.parseEmails(file);
        int sent = 0;
        for (String to : emails) {
            if (EmailValidator.isValid(to)) {
                sendSimpleEmail(to, subject, body, footer);
                sent++;
            }
        }
        return sent;
    }

    @Async
    public int sendBulkInline(
            MultipartFile excel,
            String subject,
            String bodyHtml,
            List<MultipartFile> images,
            String footer  // Nuevo parámetro footer
    ) {
        List<String> recipients = excelParser.parseEmails(excel);
        int sentCount = 0;
        for (String to : recipients) {
            if (EmailValidator.isValid(to)) {
                sendEmailWithInlineImages(to, subject, bodyHtml, images, footer);
                sentCount++;
            }
        }
        return sentCount;
    }

    @Async
    public int sendBulkWithAttachmentsAndInline(
            MultipartFile excel,
            String subject,
            String bodyHtml,
            List<MultipartFile> inlineImages,
            List<MultipartFile> attachments,
            String footer  // Nuevo parámetro footer
    ) {
        List<String> recipients = excelParser.parseEmails(excel);
        int sentCount = 0;
        for (String to : recipients) {
            if (EmailValidator.isValid(to)) {
                sendEmailWithAttachmentsAndInline(to, subject, bodyHtml, inlineImages, attachments, footer);
                sentCount++;
            }
        }
        return sentCount;
    }

    // ---------- Métodos internos con from y replyTo propios ----------

    private void sendSimpleEmail(String to, String subject, String body, String footer) {
        // Construir cuerpo completo con footer opcional y leyenda fija
        String fullBody = body;
        if (footer != null && !footer.isEmpty()) {
            fullBody += "\n\n" + footer;
        }
        fullBody += "\n\n----------------------------------------\n" + NO_REPLY_NOTICE;

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(BULK_FROM);
        msg.setReplyTo(BULK_REPLY_TO);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(fullBody);
        mailSender.send(msg);
    }

    private void sendEmailWithInlineImages(
            String to,
            String subject,
            String htmlBody,
            List<MultipartFile> inlineImages,
            String footer  // Nuevo parámetro footer
    ) {
        try {
            // Construir cuerpo HTML completo
            StringBuilder fullHtmlBody = new StringBuilder(htmlBody);

            // Añadir footer si está presente
            if (footer != null && !footer.isEmpty()) {
                fullHtmlBody.append("<br><br><div style='color: #666; font-size: 12px;'>")
                        .append(footer)
                        .append("</div>");
            }

            // Añadir leyenda fija
            fullHtmlBody.append("<br><hr><div style='color: #999; font-style: italic; font-size: 10px;'>")
                    .append(NO_REPLY_NOTICE)
                    .append("</div>");

            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(BULK_FROM);
            helper.setReplyTo(BULK_REPLY_TO);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(fullHtmlBody.toString(), true);

            if (inlineImages != null) {
                for (MultipartFile img : inlineImages) {
                    String baseName = img.getOriginalFilename();
                    if (baseName == null) continue;
                    baseName = baseName
                            .replaceAll("\\s+", "")
                            .replaceAll("\\.[^.]+$", "");
                    String cid = baseName.toLowerCase();
                    helper.addInline(
                            cid,
                            new ByteArrayResource(img.getBytes()),
                            img.getContentType()
                    );
                }
            }

            mailSender.send(msg);
        } catch (MessagingException | IOException e) {
            throw new RuntimeException("Error enviando email inline en bulk", e);
        }
    }

    private void sendEmailWithAttachmentsAndInline(
            String to,
            String subject,
            String htmlBody,
            List<MultipartFile> inlineImages,
            List<MultipartFile> attachments,
            String footer  // Nuevo parámetro footer
    ) {
        try {
            // Construir cuerpo HTML completo
            StringBuilder fullHtmlBody = new StringBuilder(htmlBody);

            // Añadir footer si está presente
            if (footer != null && !footer.isEmpty()) {
                fullHtmlBody.append("<br><br><div style='color: #666; font-size: 12px;'>")
                        .append(footer)
                        .append("</div>");
            }

            // Añadir leyenda fija
            fullHtmlBody.append("<br><hr><div style='color: #999; font-style: italic; font-size: 10px;'>")
                    .append(NO_REPLY_NOTICE)
                    .append("</div>");

            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(BULK_FROM);
            helper.setReplyTo(BULK_REPLY_TO);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(fullHtmlBody.toString(), true);

            if (inlineImages != null) {
                for (MultipartFile img : inlineImages) {
                    String original = img.getOriginalFilename();
                    if (original == null) continue;
                    String cid = original.replaceAll("\\s+", "")
                            .replaceAll("\\.[^.]+$", "")
                            .toLowerCase();
                    helper.addInline(
                            cid,
                            new ByteArrayResource(img.getBytes()),
                            img.getContentType()
                    );
                }
            }

            if (attachments != null) {
                for (MultipartFile file : attachments) {
                    String name = file.getOriginalFilename();
                    if (name == null) continue;
                    helper.addAttachment(
                            name,
                            new ByteArrayResource(file.getBytes())
                    );
                }
            }

            mailSender.send(msg);
        } catch (MessagingException | IOException e) {
            throw new RuntimeException("Error enviando correo avanzado en bulk", e);
        }
    }
}
