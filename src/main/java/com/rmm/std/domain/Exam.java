package com.rmm.std.domain;

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
public class Exam {

  private UUID id;
  private UUID courseId;
  private String title;
  private OffsetDateTime startDate;
  private OffsetDateTime endDate;
  private BigDecimal coefficient;
}
