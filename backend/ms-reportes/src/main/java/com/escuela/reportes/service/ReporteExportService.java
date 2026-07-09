package com.escuela.reportes.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReporteExportService {

    public byte[] exportarAExcel(String titulo, Map<String, Object> datos) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(titulo.substring(0, Math.min(31, titulo.length())));
            CellStyle headerStyle = crearEstiloHeader(workbook);
            int rowNum = 0;
            Row headerRow = sheet.createRow(rowNum++);
            int colNum = 0;

            if (datos != null) {
                for (String key : datos.keySet()) {
                    Cell cell = headerRow.createCell(colNum++);
                    cell.setCellValue(key);
                    cell.setCellStyle(headerStyle);
                }

                Row dataRow = sheet.createRow(rowNum++);
                colNum = 0;
                for (Object value : datos.values()) {
                    Cell cell = dataRow.createCell(colNum++);
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
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
}
