package com.rmm.std.file.pdf;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

@Component
public class PdfWriter {

  private static final int MARGIN = 40;
  private static final float FONT_SIZE = 11f;
  private static final float LINE_HEIGHT = 14f;

  public File write(String title, List<String> header, List<List<String>> rows) throws IOException {
    try (var document = new PDDocument()) {
      var page = new PDPage(PDRectangle.A4);
      document.addPage(page);

      try (var cs = new PDPageContentStream(document, page)) {
        cs.beginText();
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE);
        var startY = PDRectangle.A4.getHeight() - MARGIN;
        cs.newLineAtOffset(MARGIN, startY);
        cs.showText(title);
        cs.newLineAtOffset(0, -LINE_HEIGHT * 2);

        if (header != null && !header.isEmpty()) {
          cs.showText(String.join(" | ", header));
          cs.newLineAtOffset(0, -LINE_HEIGHT);
        }

        for (var row : rows) {
          cs.showText(String.join(" | ", row));
          cs.newLineAtOffset(0, -LINE_HEIGHT);
        }
        cs.endText();
      }

      var file = File.createTempFile("transcript-", ".pdf");
      document.save(file);
      return file;
    }
  }
}
