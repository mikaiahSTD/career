package com.rmm.std.service;

import com.rmm.std.constant.Role;
import com.rmm.std.domain.User;
import com.rmm.std.dto.ExamRequest;
import com.rmm.std.dto.ExamResponse;
import com.rmm.std.dto.PageResponse;
import com.rmm.std.exception.ForbiddenException;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.mapper.ExamMapper;
import com.rmm.std.repository.CourseRepository;
import com.rmm.std.repository.CourseTeacherRepository;
import com.rmm.std.repository.ExamRepository;
import com.rmm.std.repository.model.JExam;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ExamService {

  private final ExamRepository examRepository;
  private final CourseRepository courseRepository;
  private final CourseTeacherRepository courseTeacherRepository;
  private final ExamMapper examMapper;

  @Transactional
  public ExamResponse create(User requester, ExamRequest req) {
    assertTeacherOwnsCourse(requester, req.getCourseId());
    if (!courseRepository.existsById(req.getCourseId())) {
      throw new NotFoundException("Course not found: " + req.getCourseId());
    }
    JExam saved = examRepository.save(examMapper.toJ(examMapper.toDomain(req)));
    return examMapper.toRes(saved);
  }

  public PageResponse<ExamResponse> list(UUID courseId, Pageable pageable) {
    Page<JExam> page =
        courseId == null
            ? examRepository.findAll(pageable)
            : examRepository.findByCourseId(courseId, pageable);
    return PageResponse.from(page, page.map(examMapper::toRes).toList());
  }

  public ExamResponse get(UUID id) {
    return examMapper.toRes(getEntity(id));
  }

  @Transactional
  public ExamResponse update(User requester, UUID id, ExamRequest req) {
    JExam existing = getEntity(id);
    assertTeacherOwnsCourse(requester, req.getCourseId());
    if (!courseRepository.existsById(req.getCourseId())) {
      throw new NotFoundException("Course not found: " + req.getCourseId());
    }
    existing.setCourse(
        courseRepository
            .findById(req.getCourseId())
            .orElseThrow(() -> new NotFoundException("Course not found: " + req.getCourseId())));
    existing.setTitle(req.getTitle());
    existing.setStartDate(req.getStartDate());
    existing.setEndDate(req.getEndDate());
    existing.setCoefficient(req.getCoefficient());
    return examMapper.toRes(examRepository.save(existing));
  }

  @Transactional
  public void delete(User requester, UUID id) {
    JExam existing = getEntity(id);
    assertTeacherOwnsCourse(requester, existing.getCourse().getId());
    examRepository.deleteById(id);
  }

  private void assertTeacherOwnsCourse(User requester, UUID courseId) {
    if (requester.getRole() == Role.TEACHER
        && !courseTeacherRepository.existsByCourseIdAndTeacherId(courseId, requester.getId())) {
      throw new ForbiddenException("You do not teach this course");
    }
  }

  private JExam getEntity(UUID id) {
    return examRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Exam not found: " + id));
  }
}
