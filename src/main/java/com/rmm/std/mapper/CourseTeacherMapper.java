package com.rmm.std.mapper;

import com.rmm.std.domain.CourseTeacher;
import com.rmm.std.dto.CourseTeacherRequest;
import com.rmm.std.dto.CourseTeacherResponse;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.repository.CourseRepository;
import com.rmm.std.repository.UserRepository;
import com.rmm.std.repository.model.JCourse;
import com.rmm.std.repository.model.JCourseTeacher;
import com.rmm.std.repository.model.JUser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CourseTeacherMapper {

  private final CourseRepository courseRepository;
  private final UserRepository userRepository;

  public JCourseTeacher toJ(CourseTeacher courseTeacher) {
    if (courseTeacher == null) {
      return null;
    }
    JCourse course =
        courseRepository
            .findById(courseTeacher.getCourseId())
            .orElseThrow(
                () -> new NotFoundException("Course not found: " + courseTeacher.getCourseId()));
    JUser teacher =
        userRepository
            .findById(courseTeacher.getTeacherId())
            .orElseThrow(
                () -> new NotFoundException("User not found: " + courseTeacher.getTeacherId()));
    return JCourseTeacher.builder()
        .id(courseTeacher.getId())
        .course(course)
        .teacher(teacher)
        .build();
  }

  public CourseTeacher toDomain(JCourseTeacher jCourseTeacher) {
    if (jCourseTeacher == null) {
      return null;
    }
    return CourseTeacher.builder()
        .id(jCourseTeacher.getId())
        .courseId(jCourseTeacher.getCourse().getId())
        .teacherId(jCourseTeacher.getTeacher().getId())
        .build();
  }

  public CourseTeacher toDomain(CourseTeacherRequest req) {
    if (req == null) {
      return null;
    }
    return CourseTeacher.builder()
        .courseId(req.getCourseId())
        .teacherId(req.getTeacherId())
        .build();
  }

  public CourseTeacherResponse toRes(JCourseTeacher jCourseTeacher) {
    if (jCourseTeacher == null) {
      return null;
    }
    return CourseTeacherResponse.builder()
        .id(jCourseTeacher.getId())
        .courseId(jCourseTeacher.getCourse().getId())
        .teacherId(jCourseTeacher.getTeacher().getId())
        .build();
  }
}
