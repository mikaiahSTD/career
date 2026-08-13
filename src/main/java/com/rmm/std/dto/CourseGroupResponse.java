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
public class CourseGroupResponse {

    private UUID id;
    private UUID courseId;
    private UUID teacherId;
    private UUID groupId;
}
