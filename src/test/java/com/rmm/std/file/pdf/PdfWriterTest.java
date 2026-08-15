package com.rmm.std.file.pdf;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.util.List;
import org.junit.jupiter.api.Test;

class PdfWriterTest {

  private final PdfWriter pdfWriter = new PdfWriter();

  @Test
  void write_producesValidPdfWithTitleAndRows() throws Exception {
    var file =
        pdfWriter.write(
            "Transcript of John Doe",
            List.of("Course", "Exam", "Value", "Date"),
            List.of(List.of("Math", "Midterm", "15.00", "2024-01-01T00:00:00Z")));

    var bytes = Files.readAllBytes(file.toPath());
    var content = new String(bytes, java.nio.charset.StandardCharsets.ISO_8859_1);

    assertTrue(content.startsWith("%PDF-1.4"));
    assertTrue(content.endsWith("%%EOF"));
    assertTrue(content.contains("Transcript of John Doe"));
    assertTrue(content.contains("Math"));
    assertTrue(content.contains("15.00"));
  }

  @Test
  void escape_handlesPdfSpecialCharacters() throws Exception {
    var file =
        pdfWriter.write("T(est) \\ title", List.of("Course"), List.of(List.of("par(en)thesis")));

    var content =
        new String(Files.readAllBytes(file.toPath()), java.nio.charset.StandardCharsets.ISO_8859_1);
    assertTrue(content.contains("T\\(est\\) \\\\ title"));
    assertTrue(content.contains("par\\(en\\)thesis"));
  }
}
