package com.rmm.std.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.rmm.std.dto.GraduatesExportResponse;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.service.GraduatesExportService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class GraduatesExportControllerTest {

  @Mock private GraduatesExportService graduatesExportService;

  private GraduatesExportController controller;

  @BeforeEach
  void setUp() {
    controller = new GraduatesExportController(graduatesExportService);
  }

  @Test
  void requestExport_returns202WithMessage() {
    var promotionId = UUID.randomUUID();
    when(graduatesExportService.request(promotionId))
        .thenReturn(GraduatesExportResponse.builder().message("generation requested").build());

    var res = controller.requestExport(promotionId);

    assertEquals(HttpStatus.ACCEPTED, res.getStatusCode());
    assertEquals("generation requested", res.getBody().getMessage());
  }

  @Test
  void download_returnsXlsxBytes() {
    var promotionId = UUID.randomUUID();
    byte[] bytes = {1, 2, 3};
    when(graduatesExportService.getExportedFile(promotionId)).thenReturn(bytes);

    var res = controller.download(promotionId);

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertArrayEquals(bytes, res.getBody());
    assertTrue(res.getHeaders().getContentType().toString().contains("spreadsheetml"));
    assertTrue(res.getHeaders().getContentDisposition().toString().contains("graduates.xlsx"));
  }

  @Test
  void download_notReady_propagatesNotFound() {
    var promotionId = UUID.randomUUID();
    when(graduatesExportService.getExportedFile(promotionId))
        .thenThrow(new NotFoundException("not ready"));

    assertThrows(NotFoundException.class, () -> controller.download(promotionId));
  }
}
