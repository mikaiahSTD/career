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
public class PromotionStudentsGradesResponse {

  private UUID promotionId;
  private String promotionLabel;
  private Integer startYear;
  private List<StudentWithGrades> students;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class StudentWithGrades {

    private UUID studentId;
    private String firstname;
    private String lastname;
    private String email;
    private List<CourseGrade> courseGrades;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CourseGrade {

    private UUID courseId;
    private String courseTitle;
    private int credits;
    private String examTitle;
    private BigDecimal grade;
    private UUID examId;
  }
}
