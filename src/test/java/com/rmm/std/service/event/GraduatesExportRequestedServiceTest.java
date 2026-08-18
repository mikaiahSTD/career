package com.rmm.std.service.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rmm.std.dto.GraduateExportRow;
import com.rmm.std.endpoint.event.model.GraduatesExportRequested;
import com.rmm.std.file.bucket.BucketComponent;
import com.rmm.std.file.excel.ExcelWriter;
import com.rmm.std.service.GraduatesExportService;
import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraduatesExportRequestedServiceTest {

  @Mock private GraduatesExportService graduatesExportService;
  @Mock private ExcelWriter excelWriter;
  @Mock private BucketComponent bucketComponent;

  private GraduatesExportRequestedService service;

  @BeforeEach
  void setUp() {
    service =
        new GraduatesExportRequestedService(graduatesExportService, excelWriter, bucketComponent);
  }

  @Test
  void accept_computesGraduatesWritesRowsAndUploadsToBucket() throws Exception {
    var promotionId = UUID.randomUUID();
    var row =
        GraduateExportRow.builder()
            .rank(1)
            .ref("REF-001")
            .firstname("John")
            .lastname("Doe")
            .overallAverage(new BigDecimal("15.00"))
            .build();
    var file = File.createTempFile("graduates-", ".xlsx");

    when(graduatesExportService.computeGraduates(promotionId)).thenReturn(List.of(row));
    when(graduatesExportService.bucketKey(promotionId))
        .thenReturn("graduates-exports/" + promotionId + "/graduates.xlsx");
    when(excelWriter.writeToFile(any())).thenReturn(file);

    service.accept(GraduatesExportRequested.builder().promotionId(promotionId).build());

    ArgumentCaptor<List<List<String>>> rowsCaptor = ArgumentCaptor.forClass(List.class);
    verify(excelWriter).writeToFile(rowsCaptor.capture());
    var rows = rowsCaptor.getValue();
    assertEquals(List.of("Rank", "Ref", "Firstname", "Lastname", "Overall average"), rows.get(0));
    assertEquals(List.of("1", "REF-001", "John", "Doe", "15.00"), rows.get(1));

    verify(bucketComponent).upload(file, "graduates-exports/" + promotionId + "/graduates.xlsx");
  }
}
