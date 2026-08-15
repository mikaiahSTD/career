package com.rmm.std.mapper;

import com.rmm.std.domain.Exam;
import com.rmm.std.dto.ExamRequest;
import com.rmm.std.dto.ExamResponse;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.repository.CourseRepository;
import com.rmm.std.repository.model.JCourse;
import com.rmm.std.repository.model.JExam;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ExamMapper {

  private final CourseRepository courseRepository;

  public JExam toJ(Exam exam) {
    if (exam == null) {
      return null;
    }
    JCourse course =
        courseRepository
            .findById(exam.getCourseId())
            .orElseThrow(() -> new NotFoundException("Course not found: " + exam.getCourseId()));
    return JExam.builder()
        .id(exam.getId())
        .course(course)
        .title(exam.getTitle())
        .startDate(exam.getStartDate())
        .endDate(exam.getEndDate())
        .coefficient(exam.getCoefficient())
        .build();
  }

  public Exam toDomain(JExam jExam) {
    if (jExam == null) {
      return null;
    }
    return Exam.builder()
        .id(jExam.getId())
        .courseId(jExam.getCourse().getId())
        .title(jExam.getTitle())
        .startDate(jExam.getStartDate())
        .endDate(jExam.getEndDate())
        .coefficient(jExam.getCoefficient())
        .build();
  }

  public Exam toDomain(ExamRequest req) {
    if (req == null) {
      return null;
    }
    return Exam.builder()
        .courseId(req.getCourseId())
        .title(req.getTitle())
        .startDate(req.getStartDate())
        .endDate(req.getEndDate())
        .coefficient(req.getCoefficient())
        .build();
  }

  public ExamResponse toRes(JExam jExam) {
    if (jExam == null) {
      return null;
    }
    return ExamResponse.builder()
        .id(jExam.getId())
        .courseId(jExam.getCourse().getId())
        .title(jExam.getTitle())
        .startDate(jExam.getStartDate())
        .endDate(jExam.getEndDate())
        .coefficient(jExam.getCoefficient())
        .build();
  }
}
