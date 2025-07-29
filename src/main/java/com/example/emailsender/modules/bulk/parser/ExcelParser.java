package com.example.emailsender.modules.bulk.parser;


import com.example.emailsender.utils.excel.ExcelUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Component
public class ExcelParser {
    public List<String> parseEmails(MultipartFile file) {
        try {
            return ExcelUtils.extractEmails(file.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo Excel", e);
        }
    }
}