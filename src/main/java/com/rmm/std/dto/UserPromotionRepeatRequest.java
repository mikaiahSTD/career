package com.rmm.std.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPromotionRepeatRequest {

  @NotNull private UUID userId;

  @NotNull private UUID fromPromotionId;

  @NotNull private UUID toPromotionId;
}
