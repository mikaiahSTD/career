package com.rmm.std.file.excel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class ExcelWriter {

  public byte[] write(List<List<String>> rows) throws IOException {
    try (var workbook = new XSSFWorkbook()) {
      var sheet = workbook.createSheet("Graduates");

      for (int r = 0; r < rows.size(); r++) {
        XSSFRow row = sheet.createRow(r);
        List<String> cells = rows.get(r);
        for (int c = 0; c < cells.size(); c++) {
          row.createCell(c).setCellValue(cells.get(c));
        }
      }

      var out = new ByteArrayOutputStream();
      workbook.write(out);
      return out.toByteArray();
    }
  }
}
