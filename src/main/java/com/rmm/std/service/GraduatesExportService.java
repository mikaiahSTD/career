package com.rmm.std.service;

import com.rmm.std.constant.PromotionStatus;
import com.rmm.std.dto.GraduateExportRow;
import com.rmm.std.dto.GraduatesExportResponse;
import com.rmm.std.endpoint.event.EventProducer;
import com.rmm.std.endpoint.event.model.GraduatesExportRequested;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.file.bucket.BucketComponent;
import com.rmm.std.repository.CareerCourseRepository;
import com.rmm.std.repository.GradeRepository;
import com.rmm.std.repository.PromotionRepository;
import com.rmm.std.repository.UserGroupRepository;
import com.rmm.std.repository.UserPromotionRepository;
import com.rmm.std.repository.model.JGrade;
import com.rmm.std.repository.model.JUser;
import com.rmm.std.repository.model.JUserPromotion;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class GraduatesExportService {

  private static final BigDecimal PASSING_GRADE = new BigDecimal("10");

  private final PromotionRepository promotionRepository;
  private final UserPromotionRepository userPromotionRepository;
  private final GradeRepository gradeRepository;
  private final UserGroupRepository userGroupRepository;
  private final CareerCourseRepository careerCourseRepository;
  private final EventProducer<GraduatesExportRequested> eventProducer;
  private final BucketComponent bucketComponent;

  public GraduatesExportResponse request(UUID promotionId) {
    promotionRepository
        .findById(promotionId)
        .orElseThrow(() -> new NotFoundException("Promotion not found: " + promotionId));

    var event = GraduatesExportRequested.builder().promotionId(promotionId).build();
    eventProducer.accept(List.of(event));

    return GraduatesExportResponse.builder()
        .message("Graduates export generation requested. The file will be available shortly.")
        .build();
  }

  @Transactional(readOnly = true)
  public List<GraduateExportRow> computeGraduates(UUID promotionId) {
    promotionRepository
        .findById(promotionId)
        .orElseThrow(() -> new NotFoundException("Promotion not found: " + promotionId));

    List<StudentAverage> graduatedStudents = new ArrayList<>();
    for (JUserPromotion enrollment :
        userPromotionRepository.findByPromotionIdAndStatus(
            promotionId, PromotionStatus.GRADUATED)) {
      JUser student = enrollment.getUser();
      List<JGrade> latestByCourse =
          new ArrayList<>(
              latestGradeByCourse(gradeRepository.findByStudentIdForDegree(student.getId()))
                  .values());
      if (latestByCourse.isEmpty()) {
        continue;
      }

      int earnedCredits = 0;
      BigDecimal weightedSum = BigDecimal.ZERO;
      boolean allPassed = true;
      for (JGrade grade : latestByCourse) {
        int credits = creditsOf(grade);
        earnedCredits += credits;
        weightedSum = weightedSum.add(grade.getValue().multiply(BigDecimal.valueOf(credits)));
        if (grade.getValue().compareTo(PASSING_GRADE) < 0) {
          allPassed = false;
        }
      }

      if (!allPassed || earnedCredits < requiredCredits(student.getId(), earnedCredits)) {
        continue;
      }

      BigDecimal overallAverage =
          weightedSum.divide(BigDecimal.valueOf(earnedCredits), 2, RoundingMode.HALF_UP);
      graduatedStudents.add(new StudentAverage(student, overallAverage));
    }

    graduatedStudents.sort(
        Comparator.comparing(StudentAverage::average)
            .reversed()
            .thenComparing(s -> s.student().getLastname())
            .thenComparing(s -> s.student().getFirstname()));

    List<GraduateExportRow> rows = new ArrayList<>();
    int rank = 1;
    for (StudentAverage s : graduatedStudents) {
      JUser student = s.student();
      rows.add(
          GraduateExportRow.builder()
              .rank(rank++)
              .ref(student.getRef())
              .firstname(student.getFirstname())
              .lastname(student.getLastname())
              .overallAverage(s.average())
              .build());
    }
    return rows;
  }

  /**
   * A student is graduated only if every credit of his career has been earned. The required total
   * is the sum of the credits of every course of his career (180 credits over the 3-year program).
   * A credit is earned only when the student's overall average for the course is at least 10/20.
   * When no current group (hence no career) can be resolved, the student's own courses are used as
   * the reference.
   */
  private int requiredCredits(UUID studentId, int earnedCredits) {
    return userGroupRepository
        .findFirstByUserIdAndEndDateIsNull(studentId)
        .map(ug -> ug.getGroup().getCareer().getId())
        .map(careerCourseRepository::sumCreditsByCareerId)
        .orElse(earnedCredits);
  }

  private Map<UUID, JGrade> latestGradeByCourse(List<JGrade> allGrades) {
    Map<UUID, JGrade> latestByCourseId = new HashMap<>();
    for (JGrade grade : allGrades) {
      UUID courseId = grade.getExam().getCourse().getId();
      JGrade existing = latestByCourseId.get(courseId);
      if (existing == null) {
        latestByCourseId.put(courseId, grade);
      } else {
        var thisDate = grade.getExam().getStartDate();
        var existingDate = existing.getExam().getStartDate();
        if (thisDate != null && (existingDate == null || thisDate.isAfter(existingDate))) {
          latestByCourseId.put(courseId, grade);
        } else if (thisDate == null
            && existingDate == null
            && grade.getId().compareTo(existing.getId()) > 0) {
          latestByCourseId.put(courseId, grade);
        }
      }
    }
    return latestByCourseId;
  }

  private int creditsOf(JGrade grade) {
    Integer credits = grade.getExam().getCourse().getCredits();
    return credits != null ? credits : 0;
  }

  public String bucketKey(UUID promotionId) {
    return "graduates-exports/" + promotionId + "/graduates.xlsx";
  }

  @SneakyThrows
  public byte[] getExportedFile(UUID promotionId) {
    try {
      var file = bucketComponent.download(bucketKey(promotionId));
      return Files.readAllBytes(file.toPath());
    } catch (Exception e) {
      throw new NotFoundException(
          "Graduates export is not ready yet for promotion: " + promotionId);
    }
  }

  private record StudentAverage(JUser student, BigDecimal average) {}
}
