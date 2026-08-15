package com.rmm.std.service;

import com.rmm.std.constant.Role;
import com.rmm.std.dto.CourseGroupRequest;
import com.rmm.std.dto.CourseGroupResponse;
import com.rmm.std.dto.PageResponse;
import com.rmm.std.exception.BadRequestException;
import com.rmm.std.exception.ConflictException;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.mapper.CourseGroupMapper;
import com.rmm.std.repository.CourseGroupRepository;
import com.rmm.std.repository.CourseRepository;
import com.rmm.std.repository.GroupRepository;
import com.rmm.std.repository.UserRepository;
import com.rmm.std.repository.model.JCourseGroup;
import com.rmm.std.repository.model.JUser;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CourseGroupService {

  private final CourseGroupRepository courseGroupRepository;
  private final CourseRepository courseRepository;
  private final UserRepository userRepository;
  private final GroupRepository groupRepository;
  private final CourseGroupMapper courseGroupMapper;

  @Transactional
  public CourseGroupResponse create(CourseGroupRequest req) {
    validateReferences(req.getCourseId(), req.getTeacherId(), req.getGroupId());
    if (courseGroupRepository.existsByCourseIdAndTeacherIdAndGroupId(
        req.getCourseId(), req.getTeacherId(), req.getGroupId())) {
      throw new ConflictException(
          "This course is already assigned to this group with this teacher");
    }
    JCourseGroup saved =
        courseGroupRepository.save(courseGroupMapper.toJ(courseGroupMapper.toDomain(req)));
    return courseGroupMapper.toRes(saved);
  }

  public PageResponse<CourseGroupResponse> list(
      UUID courseId, UUID teacherId, UUID groupId, Pageable pageable) {
    Page<JCourseGroup> page = courseGroupRepository.search(courseId, teacherId, groupId, pageable);
    return PageResponse.from(page, page.map(courseGroupMapper::toRes).toList());
  }

  public CourseGroupResponse get(UUID id) {
    return courseGroupMapper.toRes(getEntity(id));
  }

  @Transactional
  public CourseGroupResponse update(UUID id, CourseGroupRequest req) {
    JCourseGroup existing = getEntity(id);
    validateReferences(req.getCourseId(), req.getTeacherId(), req.getGroupId());
    existing.setCourse(
        courseRepository
            .findById(req.getCourseId())
            .orElseThrow(() -> new NotFoundException("Course not found: " + req.getCourseId())));
    existing.setTeacher(
        userRepository
            .findById(req.getTeacherId())
            .orElseThrow(() -> new NotFoundException("User not found: " + req.getTeacherId())));
    existing.setGroup(
        groupRepository
            .findById(req.getGroupId())
            .orElseThrow(() -> new NotFoundException("Group not found: " + req.getGroupId())));
    return courseGroupMapper.toRes(courseGroupRepository.save(existing));
  }

  @Transactional
  public void delete(UUID id) {
    getEntity(id);
    courseGroupRepository.deleteById(id);
  }

  private void validateReferences(UUID courseId, UUID teacherId, UUID groupId) {
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
    if (!groupRepository.existsById(groupId)) {
      throw new NotFoundException("Group not found: " + groupId);
    }
  }

  private JCourseGroup getEntity(UUID id) {
    return courseGroupRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Course-group assignment not found: " + id));
  }
}
