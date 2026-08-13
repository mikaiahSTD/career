package com.rmm.std.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamRequest {

  @NotNull private UUID courseId;

  private String title;
  private OffsetDateTime startDate;
  private OffsetDateTime endDate;

  @NotNull
  @DecimalMin("0.0")
  @DecimalMax("9.99")
  private BigDecimal coefficient;
}
