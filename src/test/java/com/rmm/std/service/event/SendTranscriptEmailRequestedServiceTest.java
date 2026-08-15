package com.rmm.std.service.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rmm.std.endpoint.event.model.SendTranscriptEmailRequested;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.file.bucket.BucketComponent;
import com.rmm.std.file.pdf.PdfWriter;
import com.rmm.std.mail.Email;
import com.rmm.std.mail.Mailer;
import com.rmm.std.repository.GradeRepository;
import com.rmm.std.repository.SemesterRepository;
import com.rmm.std.repository.UserRepository;
import com.rmm.std.repository.model.JCourse;
import com.rmm.std.repository.model.JExam;
import com.rmm.std.repository.model.JGrade;
import com.rmm.std.repository.model.JSemester;
import com.rmm.std.repository.model.JUser;
import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.time.Duration;
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
class SendTranscriptEmailRequestedServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private GradeRepository gradeRepository;
  @Mock private SemesterRepository semesterRepository;
  @Mock private PdfWriter pdfWriter;
  @Mock private BucketComponent bucketComponent;
  @Mock private Mailer mailer;

  private SendTranscriptEmailRequestedService service;

  @BeforeEach
  void setUp() {
    service =
        new SendTranscriptEmailRequestedService(
            userRepository,
            gradeRepository,
            semesterRepository,
            pdfWriter,
            bucketComponent,
            mailer);
  }

  @Test
  void accept_withFullTranscript_uploadsPdfAndSendsEmailWithPresignedUrl() throws Exception {
    var studentId = UUID.randomUUID();
    var student = student(studentId, "john-" + studentId + "@test.com");
    var grade = grade(student, "Math", "Midterm", "15.00");
    var pdf = File.createTempFile("transcript-", ".pdf");
    var presignedUrl = new URL("https://bucket.example.com/transcript.pdf");

    when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(gradeRepository.findByStudentId(studentId)).thenReturn(List.of(grade));
    when(pdfWriter.write(any(), any(), any())).thenReturn(pdf);
    when(bucketComponent.presign(any(), any(Duration.class))).thenReturn(presignedUrl);

    service.accept(SendTranscriptEmailRequested.builder().studentId(studentId).build());

    ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
    verify(bucketComponent).upload(any(File.class), keyCaptor.capture());
    assertEquals("transcripts/" + studentId + "/" + pdf.getName(), keyCaptor.getValue());

    ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
    verify(mailer).accept(emailCaptor.capture());
    var email = emailCaptor.getValue();
    assertEquals("Your full transcript", email.subject());
    assertEquals(student.getEmail(), email.to().getAddress());
    assertContains(email.htmlBody(), presignedUrl.toString());
  }

  @Test
  void accept_withSemesterId_filtersGradesBySemester() throws Exception {
    var studentId = UUID.randomUUID();
    var semesterId = UUID.randomUUID();
    var student = student(studentId, "john-" + studentId + "@test.com");
    var grade = grade(student, "Physics", "Final", "12.50");
    var pdf = File.createTempFile("transcript-", ".pdf");
    var presignedUrl = new URL("https://bucket.example.com/transcript.pdf");

    when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(semesterRepository.findById(semesterId))
        .thenReturn(Optional.of(JSemester.builder().id(semesterId).number(2).build()));
    when(gradeRepository.findGradesForStudentInSemester(studentId, 2)).thenReturn(List.of(grade));
    when(pdfWriter.write(any(), any(), any())).thenReturn(pdf);
    when(bucketComponent.presign(any(), any(Duration.class))).thenReturn(presignedUrl);

    service.accept(
        SendTranscriptEmailRequested.builder().studentId(studentId).semesterId(semesterId).build());

    verify(gradeRepository).findGradesForStudentInSemester(studentId, 2);

    ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
    verify(mailer).accept(emailCaptor.capture());
    assertEquals("Your transcript (semester)", emailCaptor.getValue().subject());
  }

  @Test
  void accept_unknownStudent_throwsNotFound() {
    var studentId = UUID.randomUUID();
    when(userRepository.findById(studentId)).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () -> service.accept(SendTranscriptEmailRequested.builder().studentId(studentId).build()));
  }

  private JUser student(UUID id, String email) {
    return JUser.builder().id(id).firstname("John").lastname("Doe").email(email).build();
  }

  private JGrade grade(JUser student, String courseTitle, String examTitle, String value) {
    return JGrade.builder()
        .id(UUID.randomUUID())
        .student(student)
        .exam(
            JExam.builder()
                .id(UUID.randomUUID())
                .title(examTitle)
                .course(JCourse.builder().id(UUID.randomUUID()).title(courseTitle).build())
                .build())
        .value(new BigDecimal(value))
        .build();
  }

  private static void assertContains(String haystack, String needle) {
    if (!haystack.contains(needle)) {
      throw new AssertionError("Expected <" + haystack + "> to contain <" + needle + ">");
    }
  }
}
