package com.rmm.std.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeRequest {

    @NotNull
    private UUID examId;

    @NotNull
    private UUID studentId;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("20.0")
    private BigDecimal value;

    private String description;
}
