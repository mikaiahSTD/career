package com.rmm.std.domain;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareerCourse {

    private UUID id;
    private UUID careerId;
    private UUID courseId;
    private Integer semesterNumber;
}
