package com.rmm.std.mapper;

import com.rmm.std.domain.CareerCourse;
import com.rmm.std.dto.CareerCourseRequest;
import com.rmm.std.dto.CareerCourseResponse;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.repository.CareerRepository;
import com.rmm.std.repository.CourseRepository;
import com.rmm.std.repository.model.JCareer;
import com.rmm.std.repository.model.JCareerCourse;
import com.rmm.std.repository.model.JCourse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CareerCourseMapper {

  private final CareerRepository careerRepository;
  private final CourseRepository courseRepository;

  public JCareerCourse toJ(CareerCourse careerCourse) {
    if (careerCourse == null) {
      return null;
    }
    JCareer career =
        careerRepository
            .findById(careerCourse.getCareerId())
            .orElseThrow(
                () -> new NotFoundException("Career not found: " + careerCourse.getCareerId()));
    JCourse course =
        courseRepository
            .findById(careerCourse.getCourseId())
            .orElseThrow(
                () -> new NotFoundException("Course not found: " + careerCourse.getCourseId()));
    return JCareerCourse.builder()
        .id(careerCourse.getId())
        .career(career)
        .course(course)
        .semesterNumber(careerCourse.getSemesterNumber())
        .build();
  }

  public CareerCourse toDomain(JCareerCourse jCareerCourse) {
    if (jCareerCourse == null) {
      return null;
    }
    return CareerCourse.builder()
        .id(jCareerCourse.getId())
        .careerId(jCareerCourse.getCareer().getId())
        .courseId(jCareerCourse.getCourse().getId())
        .semesterNumber(jCareerCourse.getSemesterNumber())
        .build();
  }

  public CareerCourse toDomain(CareerCourseRequest req) {
    if (req == null) {
      return null;
    }
    return CareerCourse.builder()
        .careerId(req.getCareerId())
        .courseId(req.getCourseId())
        .semesterNumber(req.getSemesterNumber())
        .build();
  }

  public CareerCourseResponse toRes(JCareerCourse jCareerCourse) {
    if (jCareerCourse == null) {
      return null;
    }
    return CareerCourseResponse.builder()
        .id(jCareerCourse.getId())
        .careerId(jCareerCourse.getCareer().getId())
        .courseId(jCareerCourse.getCourse().getId())
        .semesterNumber(jCareerCourse.getSemesterNumber())
        .build();
  }
}
