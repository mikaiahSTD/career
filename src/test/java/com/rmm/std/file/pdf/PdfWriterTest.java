package com.rmm.std.file.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.util.List;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
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
    assertTrue(bytes.length > 0);

    try (PDDocument document = Loader.loadPDF(bytes)) {
      assertEquals(1, document.getNumberOfPages());
      var stripper = new PDFTextStripper();
      var text = stripper.getText(document);
      assertTrue(text.contains("Transcript of John Doe"));
      assertTrue(text.contains("Math"));
      assertTrue(text.contains("15.00"));
      assertTrue(text.contains("Course | Exam | Value | Date"));
    }
  }

  @Test
  void escape_handlesPdfSpecialCharacters() throws Exception {
    var file =
        pdfWriter.write("T(est) \\ title", List.of("Course"), List.of(List.of("par(en)thesis")));

    try (PDDocument document = Loader.loadPDF(Files.readAllBytes(file.toPath()))) {
      var text = new PDFTextStripper().getText(document);
      assertTrue(text.contains("T(est) \\ title"));
      assertTrue(text.contains("par(en)thesis"));
    }
  }
}
