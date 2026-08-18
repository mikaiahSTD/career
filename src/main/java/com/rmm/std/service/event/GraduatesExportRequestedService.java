package com.rmm.std.service.event;

import com.rmm.std.dto.GraduateExportRow;
import com.rmm.std.endpoint.event.model.GraduatesExportRequested;
import com.rmm.std.file.bucket.BucketComponent;
import com.rmm.std.file.excel.ExcelWriter;
import com.rmm.std.service.GraduatesExportService;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GraduatesExportRequestedService implements Consumer<GraduatesExportRequested> {

  private final GraduatesExportService graduatesExportService;
  private final ExcelWriter excelWriter;
  private final BucketComponent bucketComponent;

  @Override
  @SneakyThrows
  public void accept(GraduatesExportRequested event) {
    List<GraduateExportRow> graduates =
        graduatesExportService.computeGraduates(event.getPromotionId());
    File file = excelWriter.writeToFile(buildRows(graduates));
    bucketComponent.upload(file, graduatesExportService.bucketKey(event.getPromotionId()));
  }

  private List<List<String>> buildRows(List<GraduateExportRow> graduates) {
    List<List<String>> rows = new ArrayList<>();
    rows.add(List.of("Rank", "Ref", "Firstname", "Lastname", "Overall average"));
    for (GraduateExportRow graduate : graduates) {
      rows.add(
          List.of(
              String.valueOf(graduate.getRank()),
              graduate.getRef(),
              graduate.getFirstname(),
              graduate.getLastname(),
              graduate.getOverallAverage() != null
                  ? graduate.getOverallAverage().toPlainString()
                  : ""));
    }
    return rows;
  }
}
