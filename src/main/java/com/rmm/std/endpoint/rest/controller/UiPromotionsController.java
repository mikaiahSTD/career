package com.rmm.std.endpoint.rest.controller;

import com.rmm.std.service.PromotionService;
import java.util.UUID;
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
            .map(p -> new PromotionView(p.getId(), p.getLabel(), p.getStartYear()))
            .toList());
    return "promotions";
  }

  public record PromotionView(UUID id, String label, Integer startYear) {}
}
