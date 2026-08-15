package com.rmm.std.mapper;

import com.rmm.std.domain.Group;
import com.rmm.std.dto.GroupRequest;
import com.rmm.std.dto.GroupResponse;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.repository.CareerRepository;
import com.rmm.std.repository.PromotionRepository;
import com.rmm.std.repository.model.JCareer;
import com.rmm.std.repository.model.JGroup;
import com.rmm.std.repository.model.JPromotion;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GroupMapper {

  private final PromotionRepository promotionRepository;
  private final CareerRepository careerRepository;

  public JGroup toJ(Group group) {
    if (group == null) {
      return null;
    }
    JPromotion promotion =
        promotionRepository
            .findById(group.getPromotionId())
            .orElseThrow(
                () -> new NotFoundException("Promotion not found: " + group.getPromotionId()));
    JCareer career =
        careerRepository
            .findById(group.getCareerId())
            .orElseThrow(() -> new NotFoundException("Career not found: " + group.getCareerId()));
    return JGroup.builder()
        .id(group.getId())
        .ref(group.getRef())
        .promotion(promotion)
        .career(career)
        .build();
  }

  public Group toDomain(JGroup jGroup) {
    if (jGroup == null) {
      return null;
    }
    return Group.builder()
        .id(jGroup.getId())
        .ref(jGroup.getRef())
        .promotionId(jGroup.getPromotion().getId())
        .careerId(jGroup.getCareer().getId())
        .build();
  }

  public Group toDomain(GroupRequest req) {
    if (req == null) {
      return null;
    }
    return Group.builder()
        .ref(req.getRef())
        .promotionId(req.getPromotionId())
        .careerId(req.getCareerId())
        .build();
  }

  public GroupResponse toRes(JGroup jGroup) {
    if (jGroup == null) {
      return null;
    }
    return GroupResponse.builder()
        .id(jGroup.getId())
        .ref(jGroup.getRef())
        .promotionId(jGroup.getPromotion().getId())
        .careerId(jGroup.getCareer().getId())
        .build();
  }
}
