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
public class CourseGroup {

  private UUID id;
  private UUID courseId;
  private UUID teacherId;
  private UUID groupId;
}
