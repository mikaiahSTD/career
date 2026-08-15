package com.rmm.std.mapper;

import com.rmm.std.domain.Course;
import com.rmm.std.dto.CourseRequest;
import com.rmm.std.dto.CourseResponse;
import com.rmm.std.repository.model.JCourse;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

  public JCourse toJ(Course course) {
    if (course == null) {
      return null;
    }
    return JCourse.builder()
        .id(course.getId())
        .ref(course.getRef())
        .title(course.getTitle())
        .credits(course.getCredits())
        .build();
  }

  public Course toDomain(JCourse jCourse) {
    if (jCourse == null) {
      return null;
    }
    return Course.builder()
        .id(jCourse.getId())
        .ref(jCourse.getRef())
        .title(jCourse.getTitle())
        .credits(jCourse.getCredits())
        .build();
  }

  public Course toDomain(CourseRequest req) {
    if (req == null) {
      return null;
    }
    return Course.builder()
        .ref(req.getRef())
        .title(req.getTitle())
        .credits(req.getCredits())
        .build();
  }

  public CourseResponse toRes(JCourse jCourse) {
    if (jCourse == null) {
      return null;
    }
    return CourseResponse.builder()
        .id(jCourse.getId())
        .ref(jCourse.getRef())
        .title(jCourse.getTitle())
        .credits(jCourse.getCredits())
        .build();
  }
}
