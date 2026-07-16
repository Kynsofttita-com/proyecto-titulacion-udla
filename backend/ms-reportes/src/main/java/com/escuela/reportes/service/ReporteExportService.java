package com.escuela.reportes.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReporteExportService {

    private final TemplateEngine templateEngine;

    public byte[] exportarAPDF(String titulo, List<Map<String, Object>> datos) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);
            document.open();

            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD);
            Paragraph titleParagraph = new Paragraph(titulo, titleFont);
            titleParagraph.setAlignment(Element.ALIGN_CENTER);
            titleParagraph.setSpacingAfter(10);
            document.add(titleParagraph);

            com.lowagie.text.Font dateFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.ITALIC);
            Paragraph dateParagraph = new Paragraph(
                    "Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    dateFont);
            dateParagraph.setAlignment(Element.ALIGN_RIGHT);
            dateParagraph.setSpacingAfter(20);
            document.add(dateParagraph);

            if (datos != null && !datos.isEmpty()) {
                Map<String, Object> firstRow = datos.get(0);
                PdfPTable table = new PdfPTable(firstRow.size());
                table.setWidthPercentage(100);

                com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 11, com.lowagie.text.Font.BOLD, new Color(255, 255, 255));
                firstRow.keySet().forEach(key -> {
                    PdfPCell cell = new PdfPCell(new Paragraph(key, headerFont));
                    cell.setBackgroundColor(new Color(0, 0, 128));
                    cell.setPadding(8);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell);
                });

                com.lowagie.text.Font dataFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10);
                for (Map<String, Object> row : datos) {
                    for (Object value : row.values()) {
                        PdfPCell cell = new PdfPCell(new Paragraph(String.valueOf(value != null ? value : ""), dataFont));
                        cell.setPadding(5);
                        table.addCell(cell);
                    }
                }

                document.add(table);
            }

            document.close();
            log.info("Reporte exportado a PDF: {}", titulo);
            return baos.toByteArray();
        } catch (DocumentException ex) {
            log.error("Error exportando a PDF: {}", ex.getMessage(), ex);
            throw new RuntimeException("Error exportando a PDF: " + ex.getMessage());
        }
    }

    public byte[] exportarAExcel(String titulo, Map<String, Object> datos) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(titulo.substring(0, Math.min(31, titulo.length())));
            CellStyle headerStyle = crearEstiloHeader(workbook);
            int rowNum = 0;
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum++);
            int colNum = 0;

            if (datos != null) {
                for (String key : datos.keySet()) {
                    org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(colNum++);
                    cell.setCellValue(key);
                    cell.setCellStyle(headerStyle);
                }

                org.apache.poi.ss.usermodel.Row dataRow = sheet.createRow(rowNum++);
                colNum = 0;
                for (Object value : datos.values()) {
                    org.apache.poi.ss.usermodel.Cell cell = dataRow.createCell(colNum++);
                    if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                    } else if (value instanceof Boolean) {
                        cell.setCellValue((Boolean) value);
                    } else {
                        cell.setCellValue(String.valueOf(value));
                    }
                }
            }

            for (int i = 0; i < colNum; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            log.info("Reporte exportado a Excel: {}", titulo);
            return baos.toByteArray();
        } catch (IOException ex) {
            log.error("Error exportando a Excel: {}", ex.getMessage(), ex);
            throw new RuntimeException("Error exportando a Excel: " + ex.getMessage());
        }
    }

    public byte[] exportarACSV(String titulo, Map<String, Object> datos) {
        StringBuilder csv = new StringBuilder();

        if (datos != null) {
            csv.append(String.join(",", datos.keySet())).append("\n");
            csv.append(datos.values().stream()
                .map(v -> "\"" + (v != null ? v.toString().replace("\"", "\"\"") : "") + "\"")
                .reduce((a, b) -> a + "," + b)
                .orElse("")).append("\n");
        }

        log.info("Reporte exportado a CSV: {}", titulo);
        return csv.toString().getBytes();
    }

    private CellStyle crearEstiloHeader(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
}
