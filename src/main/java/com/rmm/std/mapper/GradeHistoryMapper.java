package com.rmm.std.mapper;

import com.rmm.std.domain.GradeHistory;
import com.rmm.std.dto.GradeHistoryResponse;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.repository.GradeRepository;
import com.rmm.std.repository.UserRepository;
import com.rmm.std.repository.model.JGrade;
import com.rmm.std.repository.model.JGradeHistory;
import com.rmm.std.repository.model.JUser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GradeHistoryMapper {

  private final GradeRepository gradeRepository;
  private final UserRepository userRepository;

  public JGradeHistory toJ(GradeHistory gradeHistory) {
    if (gradeHistory == null) {
      return null;
    }
    JGrade grade =
        gradeRepository
            .findById(gradeHistory.getGradeId())
            .orElseThrow(
                () -> new NotFoundException("Grade not found: " + gradeHistory.getGradeId()));
    JUser modifiedBy =
        userRepository
            .findById(gradeHistory.getModifiedById())
            .orElseThrow(
                () -> new NotFoundException("User not found: " + gradeHistory.getModifiedById()));
    return JGradeHistory.builder()
        .id(gradeHistory.getId())
        .grade(grade)
        .oldValue(gradeHistory.getOldValue())
        .newValue(gradeHistory.getNewValue())
        .reason(gradeHistory.getReason())
        .modifiedBy(modifiedBy)
        .modifiedAt(gradeHistory.getModifiedAt())
        .build();
  }

  public GradeHistory toDomain(JGradeHistory jGradeHistory) {
    if (jGradeHistory == null) {
      return null;
    }
    return GradeHistory.builder()
        .id(jGradeHistory.getId())
        .gradeId(jGradeHistory.getGrade().getId())
        .oldValue(jGradeHistory.getOldValue())
        .newValue(jGradeHistory.getNewValue())
        .reason(jGradeHistory.getReason())
        .modifiedById(jGradeHistory.getModifiedBy().getId())
        .modifiedAt(jGradeHistory.getModifiedAt())
        .build();
  }

  public GradeHistoryResponse toRes(JGradeHistory jGradeHistory) {
    if (jGradeHistory == null) {
      return null;
    }
    return GradeHistoryResponse.builder()
        .id(jGradeHistory.getId())
        .gradeId(jGradeHistory.getGrade().getId())
        .oldValue(jGradeHistory.getOldValue())
        .newValue(jGradeHistory.getNewValue())
        .reason(jGradeHistory.getReason())
        .modifiedById(jGradeHistory.getModifiedBy().getId())
        .modifiedAt(jGradeHistory.getModifiedAt())
        .build();
  }
}
