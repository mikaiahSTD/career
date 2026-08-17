package com.rmm.std.dto;

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
public class ExamResponse {

  private UUID id;
  private UUID courseId;
  private UUID semesterId;
  private String title;
  private OffsetDateTime startDate;
  private OffsetDateTime endDate;
  private BigDecimal coefficient;
}
