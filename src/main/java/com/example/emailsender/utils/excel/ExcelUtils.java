package com.example.emailsender.utils.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.WorkbookFactory;



public class ExcelUtils {
    public static List<String> extractEmails(InputStream is) {
        List<String> list = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(is)) {
            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                Cell cell = row.getCell(0);
                if (cell != null) {
                    // forzar lectura como texto si fuera numérico u otro tipo
                    cell.setCellType(CellType.STRING);
                    String email = cell.getStringCellValue().trim();
                    if (!email.isEmpty()) {
                        list.add(email);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error procesando Excel: " + e.getMessage(), e);
        }
        return list;
    }
}