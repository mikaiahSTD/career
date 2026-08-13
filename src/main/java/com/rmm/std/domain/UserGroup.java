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
public class UserGroup {

    private UUID id;
    private UUID userId;
    private UUID groupId;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
}
