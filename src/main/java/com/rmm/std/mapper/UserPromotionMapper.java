package com.rmm.std.mapper;

import com.rmm.std.domain.UserPromotion;
import com.rmm.std.dto.UserPromotionRequest;
import com.rmm.std.dto.UserPromotionResponse;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.repository.PromotionRepository;
import com.rmm.std.repository.UserRepository;
import com.rmm.std.repository.model.JPromotion;
import com.rmm.std.repository.model.JUser;
import com.rmm.std.repository.model.JUserPromotion;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserPromotionMapper {

  private final UserRepository userRepository;
  private final PromotionRepository promotionRepository;

  public JUserPromotion toJ(UserPromotion userPromotion) {
    if (userPromotion == null) {
      return null;
    }
    JUser user =
        userRepository
            .findById(userPromotion.getUserId())
            .orElseThrow(
                () -> new NotFoundException("User not found: " + userPromotion.getUserId()));
    JPromotion promotion =
        promotionRepository
            .findById(userPromotion.getPromotionId())
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Promotion not found: " + userPromotion.getPromotionId()));
    return JUserPromotion.builder()
        .id(userPromotion.getId())
        .user(user)
        .promotion(promotion)
        .graduated(userPromotion.isGraduated())
        .graduationDate(userPromotion.getGraduationDate())
        .build();
  }

  public UserPromotion toDomain(JUserPromotion jUserPromotion) {
    if (jUserPromotion == null) {
      return null;
    }
    return UserPromotion.builder()
        .id(jUserPromotion.getId())
        .userId(jUserPromotion.getUser().getId())
        .promotionId(jUserPromotion.getPromotion().getId())
        .graduated(jUserPromotion.isGraduated())
        .graduationDate(jUserPromotion.getGraduationDate())
        .build();
  }

  public UserPromotion toDomain(UserPromotionRequest req) {
    if (req == null) {
      return null;
    }
    return UserPromotion.builder()
        .userId(req.getUserId())
        .promotionId(req.getPromotionId())
        .graduated(req.isGraduated())
        .graduationDate(req.getGraduationDate())
        .build();
  }

  public UserPromotionResponse toRes(JUserPromotion jUserPromotion) {
    if (jUserPromotion == null) {
      return null;
    }
    return UserPromotionResponse.builder()
        .id(jUserPromotion.getId())
        .userId(jUserPromotion.getUser().getId())
        .promotionId(jUserPromotion.getPromotion().getId())
        .graduated(jUserPromotion.isGraduated())
        .graduationDate(jUserPromotion.getGraduationDate())
        .build();
  }
}
