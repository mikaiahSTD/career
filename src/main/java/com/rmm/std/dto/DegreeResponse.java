package com.rmm.std.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DegreeResponse {

  private UUID studentId;
  private List<CourseGrade> courseGrades;
  private int totalCredits;
  private int requiredCreditsPerSemester;
  private int requiredCreditsPerYear;
  private boolean passed;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CourseGrade {

    private UUID courseId;
    private String courseTitle;
    private int credits;
    private BigDecimal latestGrade;
    private UUID examId;
    private boolean passed;
  }
}
