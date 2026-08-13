package com.rmm.std.dto;

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
public class UserGroupRequest {

  @NotNull private UUID userId;

  @NotNull private UUID groupId;

  private OffsetDateTime startDate;
  private OffsetDateTime endDate;
}
