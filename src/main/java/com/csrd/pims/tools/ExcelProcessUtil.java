package com.csrd.pims.tools;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

import java.util.List;

public class ExcelProcessUtil {

    public static HSSFWorkbook exportExcel(String shellName, String[] titles, List<String[]> rows) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);

        HSSFSheet sheet = workbook.createSheet(shellName);
        HSSFRow title = sheet.createRow(0);
        //设置标题
        for (int i = 0; i < titles.length; i++) {
            HSSFCell cell = title.createCell(i);
            cell.setCellValue(titles[i]);
            cell.setCellStyle(cellStyle);
        }

        //设置内容
        for (int i = 0; i < rows.size(); i++) {
            String[] columns = rows.get(i);
            HSSFRow row = sheet.createRow(i + 1);
            for (int j = 0; j < columns.length; j++) {
                HSSFCell cell = row.createCell(j);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(columns[j]);
            }
        }

        return workbook;
    }

}
