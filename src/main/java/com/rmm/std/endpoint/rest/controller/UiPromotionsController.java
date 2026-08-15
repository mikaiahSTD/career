package com.rmm.std.endpoint.rest.controller;

import com.rmm.std.dto.PromotionResponse;
import com.rmm.std.service.PromotionService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
public class UiPromotionsController {

  private final PromotionService promotionService;

  @GetMapping("/ui/promotions")
  public String promotions(Model model) {
    model.addAttribute(
        "promotions",
        promotionService.list(PageRequest.of(0, 100)).getContent().stream()
            .map(PromotionResponse::getId)
            .toList());
    return "promotions";
  }
}
