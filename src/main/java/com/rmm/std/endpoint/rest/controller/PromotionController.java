package com.rmm.std.endpoint.rest.controller;

import com.rmm.std.dto.PageResponse;
import com.rmm.std.dto.PromotionRequest;
import com.rmm.std.dto.PromotionResponse;
import com.rmm.std.dto.PromotionStudentsGradesResponse;
import com.rmm.std.dto.UserResponse;
import com.rmm.std.service.PromotionService;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/promotions")
public class PromotionController {

  private final PromotionService promotionService;

  @GetMapping
  public PageResponse<PromotionResponse> list(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return promotionService.list(PageRequest.of(page, Math.min(size, 200)));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public PromotionResponse create(@RequestBody @Valid PromotionRequest req) {
    return promotionService.create(req);
  }

  @GetMapping("/{id}")
  public PromotionResponse get(@PathVariable UUID id) {
    return promotionService.get(id);
  }

  @PutMapping("/{id}")
  public PromotionResponse update(@PathVariable UUID id, @RequestBody @Valid PromotionRequest req) {
    return promotionService.update(id, req);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    promotionService.delete(id);
  }

  @GetMapping("/{id}/graduates")
  public List<UserResponse> getGraduates(@PathVariable UUID id) {
    return promotionService.getGraduates(id);
  }

  @GetMapping("/{id}/students-grades")
  public PromotionStudentsGradesResponse getStudentsGrades(@PathVariable UUID id) {
    return promotionService.getStudentsGrades(id);
  }
}
