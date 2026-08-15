package com.rmm.std.mapper;

import com.rmm.std.domain.CourseGroup;
import com.rmm.std.dto.CourseGroupRequest;
import com.rmm.std.dto.CourseGroupResponse;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.repository.CourseRepository;
import com.rmm.std.repository.GroupRepository;
import com.rmm.std.repository.UserRepository;
import com.rmm.std.repository.model.JCourse;
import com.rmm.std.repository.model.JCourseGroup;
import com.rmm.std.repository.model.JGroup;
import com.rmm.std.repository.model.JUser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CourseGroupMapper {

  private final CourseRepository courseRepository;
  private final UserRepository userRepository;
  private final GroupRepository groupRepository;

  public JCourseGroup toJ(CourseGroup courseGroup) {
    if (courseGroup == null) {
      return null;
    }
    JCourse course =
        courseRepository
            .findById(courseGroup.getCourseId())
            .orElseThrow(
                () -> new NotFoundException("Course not found: " + courseGroup.getCourseId()));
    JUser teacher =
        userRepository
            .findById(courseGroup.getTeacherId())
            .orElseThrow(
                () -> new NotFoundException("User not found: " + courseGroup.getTeacherId()));
    JGroup group =
        groupRepository
            .findById(courseGroup.getGroupId())
            .orElseThrow(
                () -> new NotFoundException("Group not found: " + courseGroup.getGroupId()));
    return JCourseGroup.builder()
        .id(courseGroup.getId())
        .course(course)
        .teacher(teacher)
        .group(group)
        .build();
  }

  public CourseGroup toDomain(JCourseGroup jCourseGroup) {
    if (jCourseGroup == null) {
      return null;
    }
    return CourseGroup.builder()
        .id(jCourseGroup.getId())
        .courseId(jCourseGroup.getCourse().getId())
        .teacherId(jCourseGroup.getTeacher().getId())
        .groupId(jCourseGroup.getGroup().getId())
        .build();
  }

  public CourseGroup toDomain(CourseGroupRequest req) {
    if (req == null) {
      return null;
    }
    return CourseGroup.builder()
        .courseId(req.getCourseId())
        .teacherId(req.getTeacherId())
        .groupId(req.getGroupId())
        .build();
  }

  public CourseGroupResponse toRes(JCourseGroup jCourseGroup) {
    if (jCourseGroup == null) {
      return null;
    }
    return CourseGroupResponse.builder()
        .id(jCourseGroup.getId())
        .courseId(jCourseGroup.getCourse().getId())
        .teacherId(jCourseGroup.getTeacher().getId())
        .groupId(jCourseGroup.getGroup().getId())
        .build();
  }
}
