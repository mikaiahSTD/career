package com.rmm.std.mapper;

import com.rmm.std.domain.Promotion;
import com.rmm.std.dto.PromotionRequest;
import com.rmm.std.dto.PromotionResponse;
import com.rmm.std.repository.model.JPromotion;
import org.springframework.stereotype.Component;

@Component
public class PromotionMapper {

  public JPromotion toJ(Promotion promotion) {
    if (promotion == null) {
      return null;
    }
    return JPromotion.builder()
        .id(promotion.getId())
        .label(promotion.getLabel())
        .startYear(promotion.getStartYear())
        .build();
  }

  public Promotion toDomain(JPromotion jPromotion) {
    if (jPromotion == null) {
      return null;
    }
    return Promotion.builder()
        .id(jPromotion.getId())
        .label(jPromotion.getLabel())
        .startYear(jPromotion.getStartYear())
        .build();
  }

  public Promotion toDomain(PromotionRequest req) {
    if (req == null) {
      return null;
    }
    return Promotion.builder().label(req.getLabel()).startYear(req.getStartYear()).build();
  }

  public PromotionResponse toRes(JPromotion jPromotion) {
    if (jPromotion == null) {
      return null;
    }
    return PromotionResponse.builder()
        .id(jPromotion.getId())
        .label(jPromotion.getLabel())
        .startYear(jPromotion.getStartYear())
        .build();
  }
}
