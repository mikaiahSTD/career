package com.rmm.std.endpoint.rest.controller;

import com.rmm.std.constant.Role;
import com.rmm.std.security.UserPrincipal;
import com.rmm.std.service.PromotionService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
public class UiController {

  private final PromotionService promotionService;

  @GetMapping("/ui/login")
  public String login() {
    return "login";
  }

  @GetMapping("/ui/signup")
  public String signup() {
    return "signup";
  }

  @GetMapping("/ui")
  public String dashboard(Model model, @AuthenticationPrincipal UserPrincipal principal) {
    if (principal.getUser().getRole() != Role.STUDENT) {
      model.addAttribute(
          "promotions",
          promotionService.list(PageRequest.of(0, 100)).getContent().stream()
              .map(p -> new PromotionDto(p.getId(), p.getLabel(), p.getStartYear()))
              .toList());
    }
    return "index";
  }

  public record PromotionDto(java.util.UUID id, String label, Integer startYear) {}
}
