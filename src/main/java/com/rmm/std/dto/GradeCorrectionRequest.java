package com.rmm.std.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeCorrectionRequest {

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("20.0")
    private BigDecimal value;

    @NotBlank
    private String reason;
}
