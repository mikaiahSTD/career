package com.rmm.std.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
public class UiPromotionsController {

  @GetMapping("/ui/promotions")
  public String promotions() {
    return "promotions";
  }
}
