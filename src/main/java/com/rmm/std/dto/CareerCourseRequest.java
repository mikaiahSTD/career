package com.rmm.std.dto;

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
public class CareerCourseRequest {

    @NotNull
    private UUID careerId;

    @NotNull
    private UUID courseId;

    @NotNull
    private Integer semesterNumber;
}
