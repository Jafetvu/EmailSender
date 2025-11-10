package com.example.emailsender.modules.bulk;

import com.example.emailsender.modules.bulk.parser.ExcelParser;
import com.example.emailsender.modules.single.dto.SafeFileDto;
import com.example.emailsender.utils.validation.EmailValidator;
import com.example.emailsender.modules.tracking.EmailTrackingService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class BulkEmailService {

    private static final Logger logger = LoggerFactory.getLogger(BulkEmailService.class);

    private final JavaMailSender mailSender;
    private final ExcelParser excelParser;
    private final EmailTemplateCache templateCache;
    private final EmailTrackingService trackingService;

    private static final String NO_REPLY_NOTICE = "Este correo es para difusión. Por favor no responder.";
    private static final String BULK_FROM = "difusion@jorgeslubricantes.com.mx";
    private static final String BULK_REPLY_TO = "noreply@jorgeslubricantes.com.mx";

    public BulkEmailService(JavaMailSender mailSender,
                            ExcelParser excelParser,
                            EmailTemplateCache templateCache,
                            EmailTrackingService trackingService) {
        this.mailSender = mailSender;
        this.excelParser = excelParser;
        this.templateCache = templateCache;
        this.trackingService = trackingService;
    }

    // ================== DTO interno cacheable ==================
    public static class ProcessedTemplate {
        private final String subject;
        private final String fullBody;
        private final String fullHtmlBody;
        private final List<SafeFileDto> inlineImages;
        private final List<SafeFileDto> attachments;

        public ProcessedTemplate(String subject,
                                 String fullBody,
                                 String fullHtmlBody,
                                 List<SafeFileDto> inlineImages,
                                 List<SafeFileDto> attachments) {
            this.subject = subject;
            this.fullBody = fullBody;
            this.fullHtmlBody = fullHtmlBody;
            this.inlineImages = inlineImages;
            this.attachments = attachments;
        }

        public String getSubject() { return subject; }
        public String getFullBody() { return fullBody; }
        public String getFullHtmlBody() { return fullHtmlBody; }
        public List<SafeFileDto> getInlineImages() { return inlineImages; }
        public List<SafeFileDto> getAttachments() { return attachments; }
    }

    // ================== Públicos ==================
    @Async
    public CompletableFuture<Integer> sendBulk(
            MultipartFile excel,
            String subject,
            String body,
            String footer
    ) {
        List<String> emails = excelParser.parseEmails(excel);
        int sent = 0;

        String fullBody = preprocessTextBody(body, footer);

        for (String to : emails) {
            if (EmailValidator.isValid(to)) {
                try {
                    sendSimpleEmailWithRetry(to, subject, fullBody);
                    sent++;
                } catch (Exception e) {
                    logger.error("Error enviando email a {}: {}", to, e.getMessage());
                }
            }
        }
        return CompletableFuture.completedFuture(sent);
    }

    @Async
    public CompletableFuture<Integer> sendBulkInline(
            MultipartFile excel,
            String subject,
            String bodyHtml,
            List<SafeFileDto> inlineImages,
            String footer
    ) {
        List<String> recipients = excelParser.parseEmails(excel);
        int sentCount = 0;

        String cacheKey = "inline_" + subject + "_" + bodyHtml.hashCode();
        ProcessedTemplate template = templateCache.get(cacheKey);

        if (template == null) {
            template = preprocessTemplate(subject, bodyHtml, footer, inlineImages, null);
            templateCache.put(cacheKey, template);
        }

        for (String to : recipients) {
            if (EmailValidator.isValid(to)) {
                try {
                    sendHtmlEmail(to, template);
                    sentCount++;
                } catch (Exception e) {
                    logger.error("Error enviando email con inline a {}: {}", to, e.getMessage());
                }
            }
        }
        return CompletableFuture.completedFuture(sentCount);
    }

    @Async
    public CompletableFuture<Integer> sendBulkWithAttachmentsAndInline(
            MultipartFile excel,
            String subject,
            String bodyHtml,
            List<SafeFileDto> inlineImages,
            List<SafeFileDto> attachments,
            String footer
    ) {
        List<String> recipients = excelParser.parseEmails(excel);
        int sentCount = 0;

        String cacheKey = "attach_" + subject + "_" + bodyHtml.hashCode();
        ProcessedTemplate template = templateCache.get(cacheKey);

        if (template == null) {
            template = preprocessTemplate(subject, bodyHtml, footer, inlineImages, attachments);
            templateCache.put(cacheKey, template);
        }

        for (String to : recipients) {
            if (EmailValidator.isValid(to)) {
                try {
                    sendHtmlEmail(to, template);
                    sentCount++;
                } catch (Exception e) {
                    logger.error("Error enviando email con adjuntos a {}: {}", to, e.getMessage());
                }
            }
        }
        return CompletableFuture.completedFuture(sentCount);
    }

    // ================== Preprocesamiento ==================
    private ProcessedTemplate preprocessTemplate(String subject,
                                                 String htmlBody,
                                                 String footer,
                                                 List<SafeFileDto> inlineImages,
                                                 List<SafeFileDto> attachments) {
        StringBuilder fullHtmlBody = new StringBuilder(htmlBody);
        if (footer != null && !footer.isEmpty()) {
            fullHtmlBody.append("<br><br><div style='color: #666; font-size: 30px;'>")
                    .append(footer)
                    .append("</div>");
        }
        fullHtmlBody.append("<br><hr><div style='color: #999; font-style: italic; font-size: 20px;'>")
                .append(NO_REPLY_NOTICE)
                .append("</div>");

        return new ProcessedTemplate(subject, null, fullHtmlBody.toString(), inlineImages, attachments);
    }

    private String preprocessTextBody(String body, String footer) {
        String fullBody = body;
        if (footer != null && !footer.isEmpty()) {
            fullBody += "\n\n" + footer;
        }
        fullBody += "\n\n----------------------------------------\n" + NO_REPLY_NOTICE;
        return fullBody;
    }

    // ================== Envío HTML ==================
    private void sendHtmlEmail(String to, ProcessedTemplate template) throws MessagingException {
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
        helper.setFrom(BULK_FROM);
        helper.setReplyTo(BULK_REPLY_TO);
        helper.setTo(to);
        helper.setSubject(template.getSubject());

        // añade el píxel de seguimiento al cuerpo HTML para cada destinatario
        String bodyWithPixel = template.getFullHtmlBody()
                + trackingService.generateTrackingPixel(to);
        helper.setText(bodyWithPixel, true);

        // Inline
        if (template.getInlineImages() != null && !template.getInlineImages().isEmpty()) {
            processInlineImages(helper, template.getInlineImages(), template.getFullHtmlBody());
        }

        // Attachments
        if (template.getAttachments() != null) {
            for (SafeFileDto file : template.getAttachments()) {
                helper.addAttachment(file.getName(), new ByteArrayResource(file.getBytes()));
            }
        }

        mailSender.send(msg);
    }

    // ================== Procesar inline (igual que EmailService) ==================
    private void processInlineImages(MimeMessageHelper helper,
                                     List<SafeFileDto> inlineImages,
                                     String htmlBody) throws MessagingException {
        Map<String, String> referencedCids = extractReferencedCids(htmlBody);
        if (referencedCids.isEmpty()) {
            assignCidsByOrder(helper, inlineImages);
        } else {
            assignCidsByReference(helper, inlineImages, referencedCids);
        }
    }

    private Map<String, String> extractReferencedCids(String htmlBody) {
        Map<String, String> cids = new HashMap<>();
        java.util.regex.Pattern pattern =
                java.util.regex.Pattern.compile("src\\s*=\\s*[\"']?cid:([^\"'\\s>]+)[\"'\\s>]");
        java.util.regex.Matcher matcher = pattern.matcher(htmlBody);
        while (matcher.find()) {
            String cid = matcher.group(1);
            cids.put(cid, cid);
        }
        return cids;
    }

    private void assignCidsByOrder(MimeMessageHelper helper,
                                   List<SafeFileDto> inlineImages) throws MessagingException {
        for (int i = 0; i < inlineImages.size(); i++) {
            SafeFileDto img = inlineImages.get(i);
            String cid = "image" + (i + 1);
            helper.addInline(cid, new ByteArrayResource(img.getBytes()), img.getContentType());
        }
    }

    private void assignCidsByReference(MimeMessageHelper helper,
                                       List<SafeFileDto> inlineImages,
                                       Map<String, String> referencedCids) throws MessagingException {
        String[] cidArray = referencedCids.keySet().toArray(new String[0]);
        for (int i = 0; i < inlineImages.size(); i++) {
            SafeFileDto img = inlineImages.get(i);
            String cid = (i < cidArray.length) ? cidArray[i] : cidArray[0] + (i + 1);
            helper.addInline(cid, new ByteArrayResource(img.getBytes()), img.getContentType());
        }
    }

    // ================== Simple email ==================
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    private void sendSimpleEmailWithRetry(String to, String subject, String fullBody) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(BULK_FROM);
        msg.setReplyTo(BULK_REPLY_TO);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(fullBody);
        mailSender.send(msg);
    }
}
