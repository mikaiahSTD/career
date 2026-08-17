package com.rmm.std.endpoint.rest.controller;

import com.rmm.std.constant.PromotionStatus;
import com.rmm.std.dto.PageResponse;
import com.rmm.std.dto.UserPromotionRepeatRequest;
import com.rmm.std.dto.UserPromotionRequest;
import com.rmm.std.dto.UserPromotionResponse;
import com.rmm.std.service.UserPromotionService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/user-promotions")
public class UserPromotionController {

  private final UserPromotionService userPromotionService;

  @GetMapping
  public PageResponse<UserPromotionResponse> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) UUID userId,
      @RequestParam(required = false) UUID promotionId,
      @RequestParam(required = false) PromotionStatus status) {
    return userPromotionService.list(
        userId, promotionId, status, PageRequest.of(page, Math.min(size, 200)));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UserPromotionResponse create(@RequestBody @Valid UserPromotionRequest req) {
    return userPromotionService.create(req);
  }

  @PostMapping("/repeat")
  @ResponseStatus(HttpStatus.CREATED)
  public UserPromotionResponse repeatYear(@RequestBody @Valid UserPromotionRepeatRequest req) {
    return userPromotionService.repeatYear(req);
  }

  @GetMapping("/{id}")
  public UserPromotionResponse get(@PathVariable UUID id) {
    return userPromotionService.get(id);
  }

  @PutMapping("/{id}")
  public UserPromotionResponse update(
      @PathVariable UUID id, @RequestBody @Valid UserPromotionRequest req) {
    return userPromotionService.update(id, req);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    userPromotionService.delete(id);
  }
}
