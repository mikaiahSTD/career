package com.rmm.std.endpoint.rest.controller;

import com.rmm.std.dto.UserResponse;
import com.rmm.std.file.excel.ExcelWriter;
import com.rmm.std.service.PromotionService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class GraduatesExportController {

  private static final String EXCEL_MEDIA_TYPE =
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

  private final PromotionService promotionService;
  private final ExcelWriter excelWriter;

  @GetMapping("/promotions/{promotionId}/graduates/export")
  public ResponseEntity<byte[]> export(@PathVariable UUID promotionId) throws IOException {
    List<UserResponse> graduates = promotionService.getGraduates(promotionId);
    List<List<String>> rows = new ArrayList<>();
    rows.add(List.of("Ref", "Firstname", "Lastname", "Email", "Role"));
    for (UserResponse graduate : graduates) {
      rows.add(
          List.of(
              graduate.getRef(),
              graduate.getFirstname(),
              graduate.getLastname(),
              graduate.getEmail(),
              graduate.getRole().name()));
    }
    byte[] content = excelWriter.write(rows);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType(EXCEL_MEDIA_TYPE));
    headers.setContentDisposition(
        ContentDisposition.attachment().filename("graduates.xlsx").build());
    return new ResponseEntity<>(content, headers, HttpStatus.OK);
  }
}
