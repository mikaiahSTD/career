package com.rmm.std.file.excel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class ExcelWriterTest {

  private final ExcelWriter excelWriter = new ExcelWriter();

  @Test
  void write_returnsNonEmptyXlsxWithRows() throws Exception {
    byte[] content = excelWriter.write(rows());

    assertTrue(content.length > 0);
    try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
      var sheet = workbook.getSheet("Graduates");
      assertNotNull(sheet);
      assertEquals("Rank", sheet.getRow(0).getCell(0).getStringCellValue());
      assertEquals("1", sheet.getRow(1).getCell(0).getStringCellValue());
      assertEquals("Doe", sheet.getRow(1).getCell(3).getStringCellValue());
    }
  }

  @Test
  void writeToFile_createsFileWithContent() throws Exception {
    File file = excelWriter.writeToFile(rows());

    assertTrue(file.exists());
    assertTrue(file.getName().startsWith("graduates-"));
    try (Workbook workbook = new XSSFWorkbook(Files.newInputStream(file.toPath()))) {
      var sheet = workbook.getSheet("Graduates");
      assertEquals("REF-001", sheet.getRow(1).getCell(1).getStringCellValue());
      assertEquals("15.00", sheet.getRow(1).getCell(4).getStringCellValue());
    } finally {
      file.delete();
    }
  }

  private List<List<String>> rows() {
    return List.of(
        List.of("Rank", "Ref", "Firstname", "Lastname", "Overall average"),
        List.of("1", "REF-001", "John", "Doe", "15.00"));
  }
}
