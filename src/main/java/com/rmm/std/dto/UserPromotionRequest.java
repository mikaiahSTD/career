package com.rmm.std.dto;

import com.rmm.std.constant.PromotionStatus;
import jakarta.validation.constraints.NotNull;
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
public class UserPromotionRequest {

  @NotNull private UUID userId;

  @NotNull private UUID promotionId;

  @Builder.Default private PromotionStatus status = PromotionStatus.IN_PROGRESS;

  private OffsetDateTime startDate;

  private OffsetDateTime endDate;

  private OffsetDateTime graduationDate;
}
