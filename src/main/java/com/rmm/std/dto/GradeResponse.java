package com.rmm.std.dto;

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
public class GradeResponse {

    private UUID id;
    private UUID examId;
    private UUID studentId;
    private BigDecimal value;
    private OffsetDateTime assignmentDate;
    private String description;
}
