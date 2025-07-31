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

    /**
     * Envía un correo HTML con imágenes inline a cada dirección extraída del Excel.
     *
     * @param excel     El archivo .xlsx con la columna de emails en la primera hoja/columna A.
     * @param subject   El asunto del mensaje.
     * @param bodyHtml  El contenido HTML del mensaje, que debe usar <img src="cid:..."> para las imágenes.
     * @param images    Lista de imágenes MultipartFile a incrustar inline (puede ser null o vacía).
     * @return Número de correos enviados exitosamente.
     */
    public int sendBulkInline(
            MultipartFile excel,
            String subject,
            String bodyHtml,
            List<MultipartFile> images
    ) {
        // 1. Extraer emails del Excel
        List<String> recipients = excelParser.parseEmails(excel);

        int sentCount = 0;
        for (String to : recipients) {
            // 2. Validar formato de email
            if (EmailValidator.isValid(to)) {
                // 3. Enviar cada email con imágenes inline
                emailService.sendEmailWithInlineImages(to, subject, bodyHtml, images);
                sentCount++;
            }
        }

        return sentCount;
    }



    /**
     * Envía un correo HTML con:
     *  - imágenes inline (p. ej. JPG/PNG)
     *  - cualquier otro adjunto (PDF, DOCX, etc.)
     * a cada dirección listada en el Excel.
     *
     * @param excel        El .xlsx con la columna A de emails.
     * @param subject      Asunto del mensaje.
     * @param bodyHtml     Contenido HTML, con <img src="cid:..."> para inline.
     * @param inlineImages Lista de imágenes que se incrustan inline (puede ser null o vacío).
     * @param attachments  Lista de archivos a adjuntar (puede ser null o vacío).
     * @return número de correos enviados.
     */
    public int sendBulkWithAttachmentsAndInline(
            MultipartFile excel,
            String subject,
            String bodyHtml,
            List<MultipartFile> inlineImages,
            List<MultipartFile> attachments
    ) {
        // 1) Extraer lista de destinatarios desde el Excel
        List<String> recipients = excelParser.parseEmails(excel);

        int sentCount = 0;
        for (String to : recipients) {
            // 2) Validar el formato de email
            if (!EmailValidator.isValid(to)) {
                continue;
            }

            // 3) Llamar al servicio de envío que soporta inline + adjuntos
            emailService.sendEmailWithAttachmentsAndInline(
                    to,
                    subject,
                    bodyHtml,
                    inlineImages,
                    attachments
            );
            sentCount++;
        }

        return sentCount;
    }
}
