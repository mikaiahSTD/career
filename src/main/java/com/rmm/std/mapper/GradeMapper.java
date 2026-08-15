package com.rmm.std.mapper;

import com.rmm.std.domain.Grade;
import com.rmm.std.dto.GradeRequest;
import com.rmm.std.dto.GradeResponse;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.repository.ExamRepository;
import com.rmm.std.repository.UserRepository;
import com.rmm.std.repository.model.JExam;
import com.rmm.std.repository.model.JGrade;
import com.rmm.std.repository.model.JUser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GradeMapper {

  private final ExamRepository examRepository;
  private final UserRepository userRepository;

  public JGrade toJ(Grade grade) {
    if (grade == null) {
      return null;
    }
    JExam exam =
        examRepository
            .findById(grade.getExamId())
            .orElseThrow(() -> new NotFoundException("Exam not found: " + grade.getExamId()));
    JUser student =
        userRepository
            .findById(grade.getStudentId())
            .orElseThrow(() -> new NotFoundException("Student not found: " + grade.getStudentId()));
    return JGrade.builder()
        .id(grade.getId())
        .exam(exam)
        .student(student)
        .value(grade.getValue())
        .assignmentDate(grade.getAssignmentDate())
        .description(grade.getDescription())
        .build();
  }

  public Grade toDomain(JGrade jGrade) {
    if (jGrade == null) {
      return null;
    }
    return Grade.builder()
        .id(jGrade.getId())
        .examId(jGrade.getExam().getId())
        .studentId(jGrade.getStudent().getId())
        .value(jGrade.getValue())
        .assignmentDate(jGrade.getAssignmentDate())
        .description(jGrade.getDescription())
        .build();
  }

  public Grade toDomain(GradeRequest req) {
    if (req == null) {
      return null;
    }
    return Grade.builder()
        .examId(req.getExamId())
        .studentId(req.getStudentId())
        .value(req.getValue())
        .description(req.getDescription())
        .build();
  }

  public GradeResponse toRes(JGrade jGrade) {
    if (jGrade == null) {
      return null;
    }
    return GradeResponse.builder()
        .id(jGrade.getId())
        .examId(jGrade.getExam().getId())
        .studentId(jGrade.getStudent().getId())
        .value(jGrade.getValue())
        .assignmentDate(jGrade.getAssignmentDate())
        .description(jGrade.getDescription())
        .build();
  }
}
