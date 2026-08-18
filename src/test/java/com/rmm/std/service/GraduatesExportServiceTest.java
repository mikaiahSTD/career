package com.rmm.std.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rmm.std.constant.PromotionStatus;
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
import com.rmm.std.repository.model.JCareer;
import com.rmm.std.repository.model.JCourse;
import com.rmm.std.repository.model.JExam;
import com.rmm.std.repository.model.JGrade;
import com.rmm.std.repository.model.JGroup;
import com.rmm.std.repository.model.JPromotion;
import com.rmm.std.repository.model.JUser;
import com.rmm.std.repository.model.JUserGroup;
import com.rmm.std.repository.model.JUserPromotion;
import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraduatesExportServiceTest {

  @Mock private PromotionRepository promotionRepository;
  @Mock private UserPromotionRepository userPromotionRepository;
  @Mock private GradeRepository gradeRepository;
  @Mock private UserGroupRepository userGroupRepository;
  @Mock private CareerCourseRepository careerCourseRepository;
  @Mock private EventProducer<GraduatesExportRequested> eventProducer;
  @Mock private BucketComponent bucketComponent;

  private GraduatesExportService service;

  @BeforeEach
  void setUp() {
    service =
        new GraduatesExportService(
            promotionRepository,
            userPromotionRepository,
            gradeRepository,
            userGroupRepository,
            careerCourseRepository,
            eventProducer,
            bucketComponent);
  }

  @Test
  void computeGraduates_allCoursesPassed_sortedByAverageAndRanked() {
    var promotionId = UUID.randomUUID();
    when(promotionRepository.findById(promotionId))
        .thenReturn(Optional.of(JPromotion.builder().id(promotionId).build()));

    var alice = student("S2", "Alice", "A");
    var bob = student("S1", "Bob", "B");
    when(userPromotionRepository.findByPromotionIdAndStatus(promotionId, PromotionStatus.GRADUATED))
        .thenReturn(List.of(enrollment(alice), enrollment(bob)));
    when(gradeRepository.findByStudentIdForDegree(alice.getId()))
        .thenReturn(
            grades(
                alice, new CourseSpec("Math", 5, "15.00"), new CourseSpec("Physics", 5, "11.00")));
    when(gradeRepository.findByStudentIdForDegree(bob.getId()))
        .thenReturn(
            grades(bob, new CourseSpec("Math", 5, "18.00"), new CourseSpec("Physics", 5, "12.00")));
    when(userGroupRepository.findFirstByUserIdAndEndDateIsNull(alice.getId()))
        .thenReturn(Optional.empty());
    when(userGroupRepository.findFirstByUserIdAndEndDateIsNull(bob.getId()))
        .thenReturn(Optional.empty());

    var rows = service.computeGraduates(promotionId);

    assertEquals(2, rows.size());
    assertEquals(1, rows.get(0).getRank());
    assertEquals("S1", rows.get(0).getRef());
    assertEquals(new BigDecimal("15.00"), rows.get(0).getOverallAverage());
    assertEquals(2, rows.get(1).getRank());
    assertEquals("S2", rows.get(1).getRef());
    assertEquals(new BigDecimal("13.00"), rows.get(1).getOverallAverage());
  }

  @Test
  void computeGraduates_courseBelowTen_notGraduated() {
    var promotionId = UUID.randomUUID();
    when(promotionRepository.findById(promotionId))
        .thenReturn(Optional.of(JPromotion.builder().id(promotionId).build()));

    var student = student("S1", "John", "Doe");
    when(userPromotionRepository.findByPromotionIdAndStatus(promotionId, PromotionStatus.GRADUATED))
        .thenReturn(List.of(enrollment(student)));
    when(gradeRepository.findByStudentIdForDegree(student.getId()))
        .thenReturn(
            grades(
                student, new CourseSpec("Math", 5, "15.00"), new CourseSpec("Physics", 5, "8.00")));

    var rows = service.computeGraduates(promotionId);

    assertTrue(rows.isEmpty());
  }

  @Test
  void computeGraduates_missingCareerCredits_notGraduated() {
    var promotionId = UUID.randomUUID();
    var careerId = UUID.randomUUID();
    when(promotionRepository.findById(promotionId))
        .thenReturn(Optional.of(JPromotion.builder().id(promotionId).build()));

    var student = student("S1", "John", "Doe");
    var career = JCareer.builder().id(careerId).build();
    var group = JGroup.builder().id(UUID.randomUUID()).career(career).build();
    var membership = JUserGroup.builder().id(UUID.randomUUID()).user(student).group(group).build();
    when(userPromotionRepository.findByPromotionIdAndStatus(promotionId, PromotionStatus.GRADUATED))
        .thenReturn(List.of(enrollment(student)));
    when(gradeRepository.findByStudentIdForDegree(student.getId()))
        .thenReturn(
            grades(
                student,
                new CourseSpec("Math", 10, "15.00"),
                new CourseSpec("Physics", 10, "12.00")));
    when(userGroupRepository.findFirstByUserIdAndEndDateIsNull(student.getId()))
        .thenReturn(Optional.of(membership));
    when(careerCourseRepository.sumCreditsByCareerId(careerId)).thenReturn(30);

    var rows = service.computeGraduates(promotionId);

    assertTrue(rows.isEmpty());
  }

  @Test
  void computeGraduates_latestGradePerCourseUsed() {
    var promotionId = UUID.randomUUID();
    when(promotionRepository.findById(promotionId))
        .thenReturn(Optional.of(JPromotion.builder().id(promotionId).build()));

    var student = student("S1", "John", "Doe");
    when(userPromotionRepository.findByPromotionIdAndStatus(promotionId, PromotionStatus.GRADUATED))
        .thenReturn(List.of(enrollment(student)));
    var course = JCourse.builder().id(UUID.randomUUID()).title("Math").credits(10).build();
    var older = grade(student, course, "First attempt", OffsetDateTime.now().minusDays(30), "8.00");
    var newer = grade(student, course, "Retake", OffsetDateTime.now(), "14.00");
    when(gradeRepository.findByStudentIdForDegree(student.getId()))
        .thenReturn(List.of(older, newer));
    when(userGroupRepository.findFirstByUserIdAndEndDateIsNull(student.getId()))
        .thenReturn(Optional.empty());

    var rows = service.computeGraduates(promotionId);

    assertEquals(1, rows.size());
    assertEquals(new BigDecimal("14.00"), rows.get(0).getOverallAverage());
  }

  @Test
  void computeGraduates_unknownPromotion_notFound() {
    var promotionId = UUID.randomUUID();
    when(promotionRepository.findById(promotionId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.computeGraduates(promotionId));
  }

  @Test
  void request_existingPromotion_producesEventAndReturnsAcceptedMessage() {
    var promotionId = UUID.randomUUID();
    when(promotionRepository.findById(promotionId))
        .thenReturn(Optional.of(JPromotion.builder().id(promotionId).build()));

    GraduatesExportResponse res = service.request(promotionId);

    assertEquals(
        "Graduates export generation requested. The file will be available shortly.",
        res.getMessage());

    ArgumentCaptor<List<GraduatesExportRequested>> captor = ArgumentCaptor.forClass(List.class);
    verify(eventProducer).accept(captor.capture());
    assertEquals(promotionId, captor.getValue().get(0).getPromotionId());
  }

  @Test
  void request_unknownPromotion_notFound() {
    var promotionId = UUID.randomUUID();
    when(promotionRepository.findById(promotionId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.request(promotionId));
  }

  @Test
  void bucketKey_isDeterministicPerPromotion() {
    var promotionId = UUID.randomUUID();

    assertEquals(
        "graduates-exports/" + promotionId + "/graduates.xlsx", service.bucketKey(promotionId));
  }

  @Test
  void getExportedFile_ready_returnsFileBytes() throws Exception {
    var promotionId = UUID.randomUUID();
    var file = File.createTempFile("graduates-", ".xlsx");
    Files.write(file.toPath(), "content".getBytes(StandardCharsets.UTF_8));
    when(bucketComponent.download("graduates-exports/" + promotionId + "/graduates.xlsx"))
        .thenReturn(file);

    var bytes = service.getExportedFile(promotionId);

    assertEquals("content", new String(bytes, StandardCharsets.UTF_8));
    file.delete();
  }

  @Test
  void getExportedFile_notReady_throwsNotFound() {
    var promotionId = UUID.randomUUID();
    when(bucketComponent.download("graduates-exports/" + promotionId + "/graduates.xlsx"))
        .thenThrow(new RuntimeException("not found"));

    assertThrows(NotFoundException.class, () -> service.getExportedFile(promotionId));
  }

  private JUser student(String ref, String firstname, String lastname) {
    return JUser.builder()
        .id(UUID.randomUUID())
        .ref(ref)
        .firstname(firstname)
        .lastname(lastname)
        .email(ref.toLowerCase() + "@test.com")
        .build();
  }

  private JUserPromotion enrollment(JUser student) {
    return JUserPromotion.builder()
        .id(UUID.randomUUID())
        .user(student)
        .status(PromotionStatus.GRADUATED)
        .build();
  }

  private List<JGrade> grades(JUser student, CourseSpec... specs) {
    return List.of(specs).stream()
        .map(
            spec ->
                grade(
                    student,
                    JCourse.builder()
                        .id(UUID.randomUUID())
                        .title(spec.title())
                        .credits(spec.credits())
                        .build(),
                    "Exam " + spec.title(),
                    OffsetDateTime.now(),
                    spec.value()))
        .toList();
  }

  private JGrade grade(
      JUser student, JCourse course, String examTitle, OffsetDateTime startDate, String value) {
    return JGrade.builder()
        .id(UUID.randomUUID())
        .student(student)
        .value(new BigDecimal(value))
        .exam(
            JExam.builder()
                .id(UUID.randomUUID())
                .title(examTitle)
                .startDate(startDate)
                .course(course)
                .build())
        .build();
  }

  private record CourseSpec(String title, int credits, String value) {}
}
