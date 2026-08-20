package com.rmm.std.service;

import com.rmm.std.constant.PromotionStatus;
import com.rmm.std.constant.Role;
import com.rmm.std.domain.User;
import com.rmm.std.dto.PageResponse;
import com.rmm.std.dto.PromotionRequest;
import com.rmm.std.dto.PromotionResponse;
import com.rmm.std.dto.PromotionStudentsGradesResponse;
import com.rmm.std.dto.UserResponse;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.mapper.PromotionMapper;
import com.rmm.std.mapper.UserMapper;
import com.rmm.std.repository.GradeRepository;
import com.rmm.std.repository.PromotionRepository;
import com.rmm.std.repository.UserPromotionRepository;
import com.rmm.std.repository.model.JGrade;
import com.rmm.std.repository.model.JPromotion;
import com.rmm.std.repository.model.JUser;
import com.rmm.std.repository.model.JUserPromotion;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class PromotionService {

  private final PromotionRepository promotionRepository;
  private final UserPromotionRepository userPromotionRepository;
  private final PromotionMapper promotionMapper;
  private final UserMapper userMapper;
  private final GradeRepository gradeRepository;

  @Transactional
  public PromotionResponse create(PromotionRequest req) {
    JPromotion saved = promotionRepository.save(promotionMapper.toJ(promotionMapper.toDomain(req)));
    return promotionMapper.toRes(saved);
  }

  public PageResponse<PromotionResponse> list(Pageable pageable) {
    Page<JPromotion> page = promotionRepository.findAll(pageable);
    return PageResponse.from(page, page.map(promotionMapper::toRes).toList());
  }

  public PromotionResponse get(UUID id) {
    return promotionMapper.toRes(getEntity(id));
  }

  @Transactional
  public PromotionResponse update(UUID id, PromotionRequest req) {
    JPromotion existing = getEntity(id);
    existing.setLabel(req.getLabel());
    existing.setStartYear(req.getStartYear());
    return promotionMapper.toRes(promotionRepository.save(existing));
  }

  @Transactional
  public void delete(UUID id) {
    getEntity(id);
    promotionRepository.deleteById(id);
  }

  @Transactional(readOnly = true)
  public List<UserResponse> getGraduates(UUID promotionId) {
    getEntity(promotionId);
    return userPromotionRepository
        .findByPromotionIdAndStatus(promotionId, PromotionStatus.GRADUATED)
        .stream()
        .map(JUserPromotion::getUser)
        .map(userMapper::toRes)
        .toList();
  }

  @Transactional(readOnly = true)
  public PromotionStudentsGradesResponse getStudentsGrades(User requester, UUID promotionId) {
    JPromotion promotion = getEntity(promotionId);

    List<JUserPromotion> enrollments = userPromotionRepository.findByPromotionId(promotionId);

    List<JGrade> grades =
        requester.getRole() == Role.TEACHER
            ? gradeRepository.findGradesByPromotionIdForTeacher(promotionId, requester.getId())
            : gradeRepository.findGradesByPromotionId(promotionId);

    Map<UUID, List<JGrade>> gradesByStudent = new LinkedHashMap<>();
    for (JGrade grade : grades) {
      UUID studentId = grade.getStudent().getId();
      gradesByStudent.computeIfAbsent(studentId, k -> new ArrayList<>()).add(grade);
    }

    List<PromotionStudentsGradesResponse.StudentWithGrades> students = new ArrayList<>();
    for (JUserPromotion enrollment : enrollments) {
      JUser user = enrollment.getUser();
      if (user.getRole() != Role.STUDENT) {
        continue;
      }
      List<JGrade> studentGrades = gradesByStudent.getOrDefault(user.getId(), List.of());
      List<PromotionStudentsGradesResponse.CourseGrade> courseGrades =
          studentGrades.stream()
              .map(
                  g ->
                      PromotionStudentsGradesResponse.CourseGrade.builder()
                          .courseId(g.getExam().getCourse().getId())
                          .courseTitle(g.getExam().getCourse().getTitle())
                          .credits(
                              g.getExam().getCourse().getCredits() != null
                                  ? g.getExam().getCourse().getCredits()
                                  : 0)
                          .examTitle(g.getExam().getTitle())
                          .grade(g.getValue())
                          .examId(g.getExam().getId())
                          .build())
              .sorted(
                  Comparator.comparing(PromotionStudentsGradesResponse.CourseGrade::getCourseTitle))
              .toList();

      students.add(
          PromotionStudentsGradesResponse.StudentWithGrades.builder()
              .studentId(user.getId())
              .firstname(user.getFirstname())
              .lastname(user.getLastname())
              .email(user.getEmail())
              .courseGrades(courseGrades)
              .build());
    }

    students.sort(
        Comparator.comparing(PromotionStudentsGradesResponse.StudentWithGrades::getLastname)
            .thenComparing(PromotionStudentsGradesResponse.StudentWithGrades::getFirstname));

    return PromotionStudentsGradesResponse.builder()
        .promotionId(promotionId)
        .promotionLabel(promotion.getLabel())
        .startYear(promotion.getStartYear())
        .students(students)
        .build();
  }

  private JPromotion getEntity(UUID id) {
    return promotionRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Promotion not found: " + id));
  }
}
