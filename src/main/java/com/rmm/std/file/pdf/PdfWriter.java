package com.rmm.std.file.pdf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PdfWriter {

  private static final int PAGE_WIDTH = 595;
  private static final int PAGE_HEIGHT = 842;
  private static final int MARGIN = 40;
  private static final int LINE_HEIGHT = 14;

  public File write(String title, List<String> header, List<List<String>> rows) throws IOException {
    var content = buildContent(title, header, rows);
    var pdf = buildPdf(content);
    var file = File.createTempFile("transcript-", ".pdf");
    Files.write(file.toPath(), pdf);
    return file;
  }

  private byte[] buildPdf(byte[] contentStream) {
    var out = new ByteArrayOutputStream();
    writeAscii(out, "%PDF-1.4\n");
    int[] offsets = new int[6];
    offsets[0] = 0;
    offsets[1] = out.size();
    writeAscii(out, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
    offsets[2] = out.size();
    writeAscii(out, "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");
    offsets[3] = out.size();
    writeAscii(
        out,
        "3 0 obj\n"
            + "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 "
            + PAGE_WIDTH
            + " "
            + PAGE_HEIGHT
            + "] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\n"
            + "endobj\n");
    offsets[4] = out.size();
    writeAscii(out, "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");
    offsets[5] = out.size();
    writeAscii(out, "5 0 obj\n<< /Length " + contentStream.length + " >>\nstream\n");
    out.writeBytes(contentStream);
    writeAscii(out, "\nendstream\nendobj\n");
    var xrefOffset = out.size();
    writeAscii(out, "xref\n0 6\n");
    writeAscii(out, "0000000000 65535 f \n");
    for (int i = 1; i <= 5; i++) {
      writeAscii(out, String.format("%010d 00000 n \n", offsets[i]));
    }
    writeAscii(out, "trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n" + xrefOffset + "\n%%EOF");
    return out.toByteArray();
  }

  private byte[] buildContent(String title, List<String> header, List<List<String>> rows) {
    var sb = new StringBuilder();
    sb.append("BT\n/F1 11 Tf\n");
    var y = PAGE_HEIGHT - MARGIN;
    sb.append(MARGIN).append(" ").append(y).append(" Td\n");
    appendTextLine(sb, title);
    y -= LINE_HEIGHT * 2;
    sb.append("0 ").append(-(LINE_HEIGHT * 2)).append(" Td\n");
    if (header != null && !header.isEmpty()) {
      appendTextLine(sb, String.join(" | ", header));
      y -= LINE_HEIGHT;
      sb.append("0 ").append(-LINE_HEIGHT).append(" Td\n");
    }
    for (var row : rows) {
      appendTextLine(sb, String.join(" | ", row));
      y -= LINE_HEIGHT;
      sb.append("0 ").append(-LINE_HEIGHT).append(" Td\n");
    }
    sb.append("ET");
    return sb.toString().getBytes(StandardCharsets.ISO_8859_1);
  }

  private void appendTextLine(StringBuilder sb, String text) {
    sb.append("(").append(escape(text)).append(") Tj\n");
  }

  private String escape(String value) {
    if (value == null) {
      return "";
    }
    return value.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
  }

  private void writeAscii(ByteArrayOutputStream out, String s) {
    out.writeBytes(s.getBytes(StandardCharsets.ISO_8859_1));
  }
}
