package com.rmm.std.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraduateExportRow {

  private int rank;
  private String ref;
  private String firstname;
  private String lastname;
  private BigDecimal overallAverage;
}
