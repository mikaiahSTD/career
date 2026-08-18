package com.rmm.std.endpoint.rest.controller;

import com.rmm.std.dto.GraduatesExportResponse;
import com.rmm.std.service.GraduatesExportService;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class GraduatesExportController {

  private static final String EXCEL_MEDIA_TYPE =
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

  private final GraduatesExportService graduatesExportService;

  @PostMapping("/promotions/{promotionId}/graduates/export")
  public ResponseEntity<GraduatesExportResponse> requestExport(@PathVariable UUID promotionId) {
    return ResponseEntity.accepted().body(graduatesExportService.request(promotionId));
  }

  @GetMapping("/promotions/{promotionId}/graduates/export")
  public ResponseEntity<byte[]> download(@PathVariable UUID promotionId) {
    byte[] content = graduatesExportService.getExportedFile(promotionId);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType(EXCEL_MEDIA_TYPE));
    headers.setContentDisposition(
        ContentDisposition.attachment().filename("graduates.xlsx").build());
    return new ResponseEntity<>(content, headers, HttpStatus.OK);
  }
}
