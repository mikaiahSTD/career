package com.rmm.std.dto;

import com.rmm.std.constant.PromotionStatus;
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
public class UserPromotionResponse {

  private UUID id;
  private UUID userId;
  private UUID promotionId;
  private PromotionStatus status;
  private OffsetDateTime startDate;
  private OffsetDateTime endDate;
  private OffsetDateTime graduationDate;
}
