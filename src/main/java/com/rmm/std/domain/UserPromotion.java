package com.rmm.std.domain;

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
public class UserPromotion {

    private UUID id;
    private UUID userId;
    private UUID promotionId;
    private boolean graduated;
    private OffsetDateTime graduationDate;
}
