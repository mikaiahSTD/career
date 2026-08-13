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
public class GradeHistory {

  private UUID id;
  private UUID gradeId;
  private BigDecimal oldValue;
  private BigDecimal newValue;
  private String reason;
  private UUID modifiedById;
  private OffsetDateTime modifiedAt;
}
