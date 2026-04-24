package org.hospital.common.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel 工具类
 */
public class ExcelUtil {

    /**
     * 读取 Excel 文件
     */
    public static List<List<String>> readExcel(MultipartFile file) throws IOException {
        List<List<String>> data = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();

            for (int i = 0; i < rowCount; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                List<String> rowData = new ArrayList<>();
                int cellCount = row.getPhysicalNumberOfCells();

                for (int j = 0; j < cellCount; j++) {
                    Cell cell = row.getCell(j);
                    rowData.add(getCellValue(cell));
                }

                data.add(rowData);
            }
        }

        return data;
    }

    /**
     * 获取单元格值
     */
    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                // 处理数字，避免科学计数法
                double num = cell.getNumericCellValue();
                if (num == (long) num) {
                    return String.valueOf((long) num);
                }
                return String.valueOf(num);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * 生成医生导入模板
     */
    public static byte[] generateDoctorTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("医生导入模板");

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"姓名*", "职称", "科室*", "医院", "手机号*", "密码*"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);

                // 设置表头样式
                CellStyle style = workbook.createCellStyle();
                style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                style.setAlignment(HorizontalAlignment.CENTER);
                cell.setCellStyle(style);

                // 设置列宽
                sheet.setColumnWidth(i, 15 * 256);
            }

            // 添加示例数据
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("张三");
            exampleRow.createCell(1).setCellValue("主治医师");
            exampleRow.createCell(2).setCellValue("神经内科");
            exampleRow.createCell(3).setCellValue("XX医院");
            exampleRow.createCell(4).setCellValue("13800138000");
            exampleRow.createCell(5).setCellValue("123456");

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 生成患者导入模板
     */
    public static byte[] generatePatientTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("患者导入模板");

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"姓名*", "性别*", "出生日期*", "手机号*", "身份证号", "密码*", "医生手机号*"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);

                CellStyle style = workbook.createCellStyle();
                style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                style.setAlignment(HorizontalAlignment.CENTER);
                cell.setCellStyle(style);

                sheet.setColumnWidth(i, 15 * 256);
            }

            // 添加示例数据
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("李四");
            exampleRow.createCell(1).setCellValue("男");
            exampleRow.createCell(2).setCellValue("1990-01-01");
            exampleRow.createCell(3).setCellValue("13900139000");
            exampleRow.createCell(4).setCellValue("110101199001011234");
            exampleRow.createCell(5).setCellValue("123456");
            exampleRow.createCell(6).setCellValue("13800138000");

            workbook.write(out);
            return out.toByteArray();
        }
    }
}