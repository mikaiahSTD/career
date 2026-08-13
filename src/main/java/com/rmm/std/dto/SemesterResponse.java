package com.rmm.std.dto;

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
public class SemesterResponse {

    private UUID id;
    private UUID promotionId;
    private Integer number;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
}
