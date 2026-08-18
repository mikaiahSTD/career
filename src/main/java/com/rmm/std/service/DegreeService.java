package com.rmm.std.service;

import com.rmm.std.dto.DegreeResponse;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.repository.GradeRepository;
import com.rmm.std.repository.UserRepository;
import com.rmm.std.repository.model.JGrade;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class DegreeService {

  private static final BigDecimal PASSING_GRADE = new BigDecimal("10");
  private static final int MAX_CREDITS_PER_SEMESTER = 30;
  private static final int MAX_CREDITS_PER_YEAR = 60;

  private final UserRepository userRepository;
  private final GradeRepository gradeRepository;

  @Transactional(readOnly = true)
  public DegreeResponse getDegree(UUID studentId) {
    userRepository
        .findById(studentId)
        .orElseThrow(() -> new NotFoundException("Student not found: " + studentId));

    List<JGrade> allGrades = gradeRepository.findByStudentIdForDegree(studentId);

    java.util.Map<UUID, JGrade> latestByCourseId = new java.util.HashMap<>();
    for (JGrade grade : allGrades) {
      UUID courseId = grade.getExam().getCourse().getId();
      JGrade existing = latestByCourseId.get(courseId);
      if (existing == null) {
        latestByCourseId.put(courseId, grade);
      } else {
        java.time.OffsetDateTime thisDate = grade.getExam().getStartDate();
        java.time.OffsetDateTime existingDate = existing.getExam().getStartDate();
        if (thisDate != null && (existingDate == null || thisDate.isAfter(existingDate))) {
          latestByCourseId.put(courseId, grade);
        } else if (thisDate == null
            && existingDate == null
            && grade.getId().compareTo(existing.getId()) > 0) {
          latestByCourseId.put(courseId, grade);
        }
      }
    }

    List<DegreeResponse.CourseGrade> courseGrades = new ArrayList<>();
    int totalCredits = 0;

    for (JGrade grade : latestByCourseId.values()) {
      UUID courseId = grade.getExam().getCourse().getId();
      String courseTitle = grade.getExam().getCourse().getTitle();
      Integer creditsObj = grade.getExam().getCourse().getCredits();
      int credits = creditsObj != null ? creditsObj : 0;
      boolean passed = grade.getValue().compareTo(PASSING_GRADE) >= 0;
      if (passed) {
        totalCredits += credits;
      }
      courseGrades.add(
          DegreeResponse.CourseGrade.builder()
              .courseId(courseId)
              .courseTitle(courseTitle)
              .credits(credits)
              .latestGrade(grade.getValue())
              .examId(grade.getExam().getId())
              .passed(passed)
              .build());
    }

    courseGrades.sort(Comparator.comparing(DegreeResponse.CourseGrade::getCourseId));

    boolean allPassed =
        !courseGrades.isEmpty()
            && courseGrades.stream().allMatch(DegreeResponse.CourseGrade::isPassed);

    return DegreeResponse.builder()
        .studentId(studentId)
        .courseGrades(courseGrades)
        .totalCredits(totalCredits)
        .requiredCreditsPerSemester(MAX_CREDITS_PER_SEMESTER)
        .requiredCreditsPerYear(MAX_CREDITS_PER_YEAR)
        .passed(allPassed)
        .build();
  }
}
