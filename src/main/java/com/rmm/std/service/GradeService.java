package com.rmm.std.service;

import com.rmm.std.constant.Role;
import com.rmm.std.domain.Grade;
import com.rmm.std.domain.User;
import com.rmm.std.dto.GradeCorrectionRequest;
import com.rmm.std.dto.GradeHistoryResponse;
import com.rmm.std.dto.GradeRequest;
import com.rmm.std.dto.GradeResponse;
import com.rmm.std.dto.PageResponse;
import com.rmm.std.exception.ConflictException;
import com.rmm.std.exception.ForbiddenException;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.mapper.GradeHistoryMapper;
import com.rmm.std.mapper.GradeMapper;
import com.rmm.std.repository.ExamRepository;
import com.rmm.std.repository.GradeHistoryRepository;
import com.rmm.std.repository.GradeRepository;
import com.rmm.std.repository.UserRepository;
import com.rmm.std.repository.model.JGrade;
import com.rmm.std.repository.model.JGradeHistory;
import com.rmm.std.repository.model.JUser;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class GradeService {

  private final GradeRepository gradeRepository;
  private final GradeHistoryRepository gradeHistoryRepository;
  private final ExamRepository examRepository;
  private final UserRepository userRepository;
  private final GradeMapper gradeMapper;
  private final GradeHistoryMapper gradeHistoryMapper;

  @Transactional
  public GradeResponse create(User requester, GradeRequest req) {
    if (requester.getRole() == Role.TEACHER
        && !gradeRepository.existsExamByTeacherTeachesCourse(req.getExamId(), requester.getId())) {
      throw new ForbiddenException("You do not teach the course of this exam");
    }
    if (!examRepository.existsById(req.getExamId())) {
      throw new NotFoundException("Exam not found: " + req.getExamId());
    }
    if (!userRepository.existsById(req.getStudentId())) {
      throw new NotFoundException("Student not found: " + req.getStudentId());
    }
    if (gradeRepository.existsByExamIdAndStudentId(req.getExamId(), req.getStudentId())) {
      throw new ConflictException("A grade already exists for this exam and student");
    }
    Grade grade = gradeMapper.toDomain(req);
    grade.setAssignmentDate(OffsetDateTime.now());
    JGrade saved = gradeRepository.save(gradeMapper.toJ(grade));
    return gradeMapper.toRes(saved);
  }

  public PageResponse<GradeResponse> list(
      User requester, UUID examId, UUID studentId, Pageable pageable) {
    Page<JGrade> page;
    if (requester.getRole() == Role.ADMIN) {
      page = gradeRepository.search(examId, studentId, pageable);
    } else if (requester.getRole() == Role.TEACHER) {
      page = gradeRepository.searchForTeacher(requester.getId(), examId, studentId, pageable);
    } else {
      throw new ForbiddenException(
          "Students must use GET /users/me/grades to view their own grades");
    }
    return PageResponse.from(page, page.map(gradeMapper::toRes).toList());
  }

  public GradeResponse get(User requester, UUID id) {
    JGrade grade = getEntity(id);
    assertCanAccess(requester, grade);
    return gradeMapper.toRes(grade);
  }

  @Transactional
  public void delete(UUID id) {
    getEntity(id);
    gradeRepository.deleteById(id);
  }

  @Transactional
  public GradeResponse correct(User requester, UUID id, GradeCorrectionRequest req) {
    JGrade grade = getEntity(id);
    assertCanModify(requester, grade);
    JUser modifiedBy =
        userRepository
            .findById(requester.getId())
            .orElseThrow(() -> new NotFoundException("User not found: " + requester.getId()));
    gradeHistoryRepository.save(
        JGradeHistory.builder()
            .grade(grade)
            .oldValue(grade.getValue())
            .newValue(req.getValue())
            .reason(req.getReason())
            .modifiedBy(modifiedBy)
            .modifiedAt(OffsetDateTime.now())
            .build());
    grade.setValue(req.getValue());
    grade.setAssignmentDate(OffsetDateTime.now());
    return gradeMapper.toRes(gradeRepository.save(grade));
  }

  public List<GradeHistoryResponse> history(User requester, UUID id) {
    JGrade grade = getEntity(id);
    assertCanAccess(requester, grade);
    return gradeHistoryRepository.findByGradeIdOrderByModifiedAtDesc(id).stream()
        .map(gradeHistoryMapper::toRes)
        .toList();
  }

  private void assertCanAccess(User requester, JGrade grade) {
    if (requester.getRole() == Role.ADMIN) {
      return;
    }
    if (requester.getRole() == Role.TEACHER) {
      if (gradeRepository.existsByIdAndTeacherTeachesCourse(grade.getId(), requester.getId())) {
        return;
      }
      throw new ForbiddenException("You do not teach the course of this grade");
    }
    if (requester.getRole() == Role.STUDENT
        && grade.getStudent().getId().equals(requester.getId())) {
      return;
    }
    throw new ForbiddenException("You are not allowed to access this grade");
  }

  private void assertCanModify(User requester, JGrade grade) {
    if (requester.getRole() == Role.ADMIN) {
      return;
    }
    if (requester.getRole() == Role.TEACHER) {
      if (gradeRepository.existsByIdAndTeacherTeachesCourse(grade.getId(), requester.getId())) {
        return;
      }
      throw new ForbiddenException("You do not teach the course of this grade");
    }
    throw new ForbiddenException("You are not allowed to modify this grade");
  }

  private JGrade getEntity(UUID id) {
    return gradeRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Grade not found: " + id));
  }
}
