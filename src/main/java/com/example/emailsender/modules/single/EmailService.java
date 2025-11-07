package com.example.emailsender.modules.single;

import com.example.emailsender.modules.single.dto.SafeFileDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private static final String NO_REPLY_NOTICE = "Este correo es para difusión. Por favor no responder.";

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendSimpleEmail(String to, String subject, String body, String footer) {
        String fullBody = body;
        if (footer != null && !footer.isEmpty()) {
            fullBody += "\n\n" + footer;
        }
        fullBody += "\n\n----------------------------------------\n" + NO_REPLY_NOTICE;

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("difusion@jorgeslubricantes.com.mx");
        msg.setReplyTo("noreply@jorgeslubricantes.com.mx");
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(fullBody);

        mailSender.send(msg);
    }

    @Async
    public void sendEmailWithInlineImages(
            String to,
            String subject,
            String htmlBody,
            List<SafeFileDto> inlineImages,
            String footer
    ) {
        sendHtmlEmail(to, subject, htmlBody, inlineImages, null, footer);
    }

    @Async
    public void sendEmailWithAttachmentsAndInline(
            String to,
            String subject,
            String htmlBody,
            List<SafeFileDto> inlineImages,
            List<SafeFileDto> attachments,
            String footer
    ) {
        sendHtmlEmail(to, subject, htmlBody, inlineImages, attachments, footer);
    }

    private void sendHtmlEmail(
            String to,
            String subject,
            String htmlBody,
            List<SafeFileDto> inlineImages,
            List<SafeFileDto> attachments,
            String footer
    ) {
        try {
            // Cuerpo HTML
            StringBuilder fullHtmlBody = new StringBuilder(htmlBody);
            if (footer != null && !footer.isEmpty()) {
                fullHtmlBody.append("<br><br><div style='color: #666; font-size: 12px;'>")
                        .append(footer)
                        .append("</div>");
            }
            fullHtmlBody.append("<br><hr><div style='color: #999; font-style: italic; font-size: 10px;'>")
                    .append(NO_REPLY_NOTICE)
                    .append("</div>");

            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom("difusion@jorgeslubricantes.com.mx");
            helper.setReplyTo("noreply@jorgeslubricantes.com.mx");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(fullHtmlBody.toString(), true);

            // Inline
            if (inlineImages != null && !inlineImages.isEmpty()) {
                processInlineImages(helper, inlineImages, htmlBody);
            }

            // Adjuntos
            if (attachments != null) {
                for (SafeFileDto file : attachments) {
                    helper.addAttachment(file.getName(), new ByteArrayResource(file.getBytes()));
                }
            }

            mailSender.send(msg);
        } catch (MessagingException e) {
            throw new RuntimeException("Error enviando correo", e);
        }
    }

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
}
