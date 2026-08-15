package com.rmm.std.service.event;

import com.rmm.std.endpoint.event.model.SendTranscriptEmailRequested;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.file.bucket.BucketComponent;
import com.rmm.std.file.pdf.PdfWriter;
import com.rmm.std.mail.Email;
import com.rmm.std.mail.Mailer;
import com.rmm.std.repository.GradeRepository;
import com.rmm.std.repository.SemesterRepository;
import com.rmm.std.repository.UserRepository;
import com.rmm.std.repository.model.JGrade;
import com.rmm.std.repository.model.JUser;
import jakarta.mail.internet.InternetAddress;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class SendTranscriptEmailRequestedService implements Consumer<SendTranscriptEmailRequested> {

  private final UserRepository userRepository;
  private final GradeRepository gradeRepository;
  private final SemesterRepository semesterRepository;
  private final PdfWriter pdfWriter;
  private final BucketComponent bucketComponent;
  private final Mailer mailer;

  @Override
  @Transactional
  @SneakyThrows
  public void accept(SendTranscriptEmailRequested event) {
    JUser student =
        userRepository
            .findById(event.getStudentId())
            .orElseThrow(() -> new NotFoundException("Student not found: " + event.getStudentId()));

    List<JGrade> grades = gradesOf(student.getId(), event.getSemesterId());
    var pdf = pdfWriter.write(buildTitle(student), header(), rows(grades));
    var bucketKey = "transcripts/" + student.getId() + "/" + pdf.getName();
    bucketComponent.upload(pdf, bucketKey);
    var presignedUrl = bucketComponent.presign(bucketKey, Duration.ofMinutes(5));

    mailer.accept(
        new Email(
            toAddress(student.getEmail()),
            List.of(),
            List.of(),
            subject(event.getSemesterId() == null),
            buildHtml(student, presignedUrl.toString()),
            List.of()));
  }

  private List<JGrade> gradesOf(UUID studentId, UUID semesterId) {
    if (semesterId == null) {
      return gradeRepository.findByStudentId(studentId);
    }
    Integer semesterNumber =
        semesterRepository
            .findById(semesterId)
            .orElseThrow(() -> new NotFoundException("Semester not found: " + semesterId))
            .getNumber();
    return gradeRepository.findGradesForStudentInSemester(studentId, semesterNumber);
  }

  private String subject(boolean fullTranscript) {
    return fullTranscript ? "Your full transcript" : "Your transcript (semester)";
  }

  private String buildTitle(JUser student) {
    return "Transcript of " + student.getFirstname() + " " + student.getLastname();
  }

  private List<String> header() {
    return List.of("Course", "Exam", "Value", "Date");
  }

  private List<List<String>> rows(List<JGrade> grades) {
    return grades.stream()
        .map(
            grade ->
                List.of(
                    grade.getExam().getCourse().getTitle(),
                    grade.getExam().getTitle(),
                    grade.getValue().toPlainString(),
                    String.valueOf(grade.getAssignmentDate())))
        .toList();
  }

  private String buildHtml(JUser student, String presignedUrl) {
    return "<html><body>"
        + "<h2>Your transcript</h2>"
        + "<p>Dear "
        + student.getFirstname()
        + " "
        + student.getLastname()
        + ",</p>"
        + "<p>Your transcript is available. Download it from the link below "
        + "(valid for 5 minutes):</p>"
        + "<p><a href=\""
        + presignedUrl
        + "\">Download transcript</a></p>"
        + "</body></html>";
  }

  @SneakyThrows
  private InternetAddress toAddress(String email) {
    return new InternetAddress(email);
  }
}
