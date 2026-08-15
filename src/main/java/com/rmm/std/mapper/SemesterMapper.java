package com.rmm.std.mapper;

import com.rmm.std.domain.Semester;
import com.rmm.std.dto.SemesterRequest;
import com.rmm.std.dto.SemesterResponse;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.repository.PromotionRepository;
import com.rmm.std.repository.model.JPromotion;
import com.rmm.std.repository.model.JSemester;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SemesterMapper {

  private final PromotionRepository promotionRepository;

  public JSemester toJ(Semester semester) {
    if (semester == null) {
      return null;
    }
    JPromotion promotion =
        promotionRepository
            .findById(semester.getPromotionId())
            .orElseThrow(
                () -> new NotFoundException("Promotion not found: " + semester.getPromotionId()));
    return JSemester.builder()
        .id(semester.getId())
        .promotion(promotion)
        .number(semester.getNumber())
        .startDate(semester.getStartDate())
        .endDate(semester.getEndDate())
        .build();
  }

  public Semester toDomain(JSemester jSemester) {
    if (jSemester == null) {
      return null;
    }
    return Semester.builder()
        .id(jSemester.getId())
        .promotionId(jSemester.getPromotion().getId())
        .number(jSemester.getNumber())
        .startDate(jSemester.getStartDate())
        .endDate(jSemester.getEndDate())
        .build();
  }

  public Semester toDomain(SemesterRequest req) {
    if (req == null) {
      return null;
    }
    return Semester.builder()
        .promotionId(req.getPromotionId())
        .number(req.getNumber())
        .startDate(req.getStartDate())
        .endDate(req.getEndDate())
        .build();
  }

  public SemesterResponse toRes(JSemester jSemester) {
    if (jSemester == null) {
      return null;
    }
    return SemesterResponse.builder()
        .id(jSemester.getId())
        .promotionId(jSemester.getPromotion().getId())
        .number(jSemester.getNumber())
        .startDate(jSemester.getStartDate())
        .endDate(jSemester.getEndDate())
        .build();
  }
}
