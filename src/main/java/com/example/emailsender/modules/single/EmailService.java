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

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendSimpleEmail(String to, String subject, String body) {
        SimpleMailMessage msg = new SimpleMailMessage();

        // Establece el remitente y el reply-to explícitamente:
        msg.setFrom("difusion@jorgeslubricantes.com.mx");
        msg.setReplyTo("noreply@jorgeslubricantes.com.mx");

        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);

        mailSender.send(msg);
    }



    @Async
    public void sendEmailWithInlineImages(
            String to,
            String subject,
            String htmlBody,
            List<MultipartFile> inlineImages
    ) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom("difusion@jorgeslubricantes.com.mx");
            helper.setReplyTo("noreply@jorgeslubricantes.com.mx");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            if (inlineImages != null) {
                for (MultipartFile img : inlineImages) {
                    // 1) Genera un Content-ID sin extensión
                    String baseName = img.getOriginalFilename()
                            .replaceAll("\\s+","")        // quita espacios
                            .replaceAll("\\.[^.]+$","");  // quita extensión
                    String cid = baseName.toLowerCase();     // p.ej. "promo"

                    // 2) Incrusta inline con ese CID
                    helper.addInline(
                            cid,
                            new ByteArrayResource(img.getBytes()),
                            img.getContentType()
                    );
                }
            }

            mailSender.send(msg);
        } catch (MessagingException|IOException e) {
            throw new RuntimeException("Error enviando email inline", e);
        }
    }



    @Async
    public void sendEmailWithAttachmentsAndInline(
            String to,
            String subject,
            String htmlBody,
            List<MultipartFile> inlineImages,
            List<MultipartFile> attachments
    ) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom("difusion@jorgeslubricantes.com.mx");
            helper.setReplyTo("noreply@jorgeslubricantes.com.mx");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            if (inlineImages != null) {
                for (MultipartFile img : inlineImages) {
                    String original = img.getOriginalFilename();
                    if (original == null) continue;
                    // Generar contentId sin extensión/spacios
                    String cid = original.replaceAll("\\s+","")
                            .replaceAll("\\.[^.]+$","")
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
            throw new RuntimeException("Error enviando correo avanzado", e);
        }
    }

}
