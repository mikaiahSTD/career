package com.rmm.std.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareerCourseResponse {

    private UUID id;
    private UUID careerId;
    private UUID courseId;
    private Integer semesterNumber;
}
