package com.rmm.std.file.excel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.stereotype.Component;

@Component
public class ExcelWriter {

  public byte[] write(List<List<String>> rows) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ZipOutputStream zip = new ZipOutputStream(baos)) {
      addEntry(zip, "[Content_Types].xml", contentTypes());
      addEntry(zip, "_rels/.rels", rootRels());
      addEntry(zip, "xl/workbook.xml", workbook());
      addEntry(zip, "xl/_rels/workbook.xml.rels", workbookRels());
      addEntry(zip, "xl/worksheets/sheet1.xml", sheet(rows));
    }
    return baos.toByteArray();
  }

  private void addEntry(ZipOutputStream zip, String name, String content) throws IOException {
    zip.putNextEntry(new ZipEntry(name));
    zip.write(content.getBytes(StandardCharsets.UTF_8));
    zip.closeEntry();
  }

  private String contentTypes() {
    return """
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
<Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
</Types>""";
  }

  private String rootRels() {
    return """
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>""";
  }

  private String workbook() {
    return """
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
<sheets><sheet name="Graduates" sheetId="1" r:id="rId1"/></sheets>
</workbook>""";
  }

  private String workbookRels() {
    return """
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
</Relationships>""";
  }

  private String sheet(List<List<String>> rows) {
    StringBuilder sb = new StringBuilder();
    sb.append(
        """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
        <sheetData>""");
    for (int r = 0; r < rows.size(); r++) {
      sb.append("<row r=\"").append(r + 1).append("\">");
      List<String> row = rows.get(r);
      for (int c = 0; c < row.size(); c++) {
        String cellRef = columnName(c) + (r + 1);
        sb.append("<c r=\"")
            .append(cellRef)
            .append("\" t=\"inlineStr\"><is><t>")
            .append(escape(row.get(c)))
            .append("</t></is></c>");
      }
      sb.append("</row>");
    }
    sb.append("</sheetData></worksheet>");
    return sb.toString();
  }

  private String columnName(int index) {
    StringBuilder sb = new StringBuilder();
    int i = index;
    while (i >= 0) {
      sb.insert(0, (char) ('A' + i % 26));
      i = i / 26 - 1;
    }
    return sb.toString();
  }

  private String escape(String value) {
    if (value == null) {
      return "";
    }
    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;");
  }
}
