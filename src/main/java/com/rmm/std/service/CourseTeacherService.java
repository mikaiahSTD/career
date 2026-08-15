package com.rmm.std.service;

import com.rmm.std.constant.Role;
import com.rmm.std.dto.CourseTeacherRequest;
import com.rmm.std.dto.CourseTeacherResponse;
import com.rmm.std.dto.PageResponse;
import com.rmm.std.exception.BadRequestException;
import com.rmm.std.exception.ConflictException;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.mapper.CourseTeacherMapper;
import com.rmm.std.repository.CourseRepository;
import com.rmm.std.repository.CourseTeacherRepository;
import com.rmm.std.repository.UserRepository;
import com.rmm.std.repository.model.JCourseTeacher;
import com.rmm.std.repository.model.JUser;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CourseTeacherService {

  private final CourseTeacherRepository courseTeacherRepository;
  private final CourseRepository courseRepository;
  private final UserRepository userRepository;
  private final CourseTeacherMapper courseTeacherMapper;

  @Transactional
  public CourseTeacherResponse create(CourseTeacherRequest req) {
    validateReferences(req.getCourseId(), req.getTeacherId());
    if (courseTeacherRepository.existsByCourseIdAndTeacherId(
        req.getCourseId(), req.getTeacherId())) {
      throw new ConflictException("Teacher is already assigned to this course");
    }
    JCourseTeacher saved =
        courseTeacherRepository.save(courseTeacherMapper.toJ(courseTeacherMapper.toDomain(req)));
    return courseTeacherMapper.toRes(saved);
  }

  public PageResponse<CourseTeacherResponse> list(
      UUID courseId, UUID teacherId, Pageable pageable) {
    Page<JCourseTeacher> page = courseTeacherRepository.search(courseId, teacherId, pageable);
    return PageResponse.from(page, page.map(courseTeacherMapper::toRes).toList());
  }

  public CourseTeacherResponse get(UUID id) {
    return courseTeacherMapper.toRes(getEntity(id));
  }

  @Transactional
  public CourseTeacherResponse update(UUID id, CourseTeacherRequest req) {
    JCourseTeacher existing = getEntity(id);
    validateReferences(req.getCourseId(), req.getTeacherId());
    existing.setCourse(
        courseRepository
            .findById(req.getCourseId())
            .orElseThrow(() -> new NotFoundException("Course not found: " + req.getCourseId())));
    existing.setTeacher(
        userRepository
            .findById(req.getTeacherId())
            .orElseThrow(() -> new NotFoundException("User not found: " + req.getTeacherId())));
    return courseTeacherMapper.toRes(courseTeacherRepository.save(existing));
  }

  @Transactional
  public void delete(UUID id) {
    getEntity(id);
    courseTeacherRepository.deleteById(id);
  }

  private void validateReferences(UUID courseId, UUID teacherId) {
    if (!courseRepository.existsById(courseId)) {
      throw new NotFoundException("Course not found: " + courseId);
    }
    JUser teacher =
        userRepository
            .findById(teacherId)
            .orElseThrow(() -> new NotFoundException("User not found: " + teacherId));
    if (teacher.getRole() != Role.TEACHER) {
      throw new BadRequestException("User " + teacherId + " is not a teacher");
    }
  }

  private JCourseTeacher getEntity(UUID id) {
    return courseTeacherRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Course-teacher assignment not found: " + id));
  }
}
