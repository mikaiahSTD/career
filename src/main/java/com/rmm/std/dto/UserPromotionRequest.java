package com.rmm.std.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPromotionRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private UUID promotionId;

    @Builder.Default
    private boolean graduated = false;

    private OffsetDateTime graduationDate;
}
