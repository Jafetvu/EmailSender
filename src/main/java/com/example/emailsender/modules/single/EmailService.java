package com.example.emailsender.modules.single;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
@Service
public class EmailService {
    private final JavaMailSender mailSender;
    // Leyenda fija implícita
    private static final String NO_REPLY_NOTICE = "Este correo es para difusión. Por favor no responder.";

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendSimpleEmail(String to, String subject, String body, String footer) {
        // Construir cuerpo completo con footer opcional y leyenda fija
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
            List<MultipartFile> inlineImages,
            String footer // Nuevo parámetro para footer
    ) {
        sendHtmlEmail(to, subject, htmlBody, inlineImages, null, footer);
    }

    @Async
    public void sendEmailWithAttachmentsAndInline(
            String to,
            String subject,
            String htmlBody,
            List<MultipartFile> inlineImages,
            List<MultipartFile> attachments,
            String footer // Nuevo parámetro para footer
    ) {
        sendHtmlEmail(to, subject, htmlBody, inlineImages, attachments, footer);
    }

    private void sendHtmlEmail(
            String to,
            String subject,
            String htmlBody,
            List<MultipartFile> inlineImages,
            List<MultipartFile> attachments,
            String footer
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
            helper.setFrom("difusion@jorgeslubricantes.com.mx");
            helper.setReplyTo("noreply@jorgeslubricantes.com.mx");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(fullHtmlBody.toString(), true);

            if (inlineImages != null) {
                for (MultipartFile img : inlineImages) {
                    String original = img.getOriginalFilename();
                    if (original == null) continue;

                    String cid = original
                            .replaceAll("\\s+", "")
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
            throw new RuntimeException("Error enviando correo", e);
        }
    }
}