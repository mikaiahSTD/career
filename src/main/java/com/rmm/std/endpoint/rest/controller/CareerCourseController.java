package com.rmm.std.endpoint.rest.controller;

import com.rmm.std.dto.CareerCourseRequest;
import com.rmm.std.dto.CareerCourseResponse;
import com.rmm.std.dto.PageResponse;
import com.rmm.std.service.CareerCourseService;
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
@RequestMapping("/career-courses")
public class CareerCourseController {

  private final CareerCourseService careerCourseService;

  @GetMapping
  public PageResponse<CareerCourseResponse> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) UUID careerId) {
    return careerCourseService.list(careerId, PageRequest.of(page, Math.min(size, 200)));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CareerCourseResponse create(@RequestBody @Valid CareerCourseRequest req) {
    return careerCourseService.create(req);
  }

  @GetMapping("/{id}")
  public CareerCourseResponse get(@PathVariable UUID id) {
    return careerCourseService.get(id);
  }

  @PutMapping("/{id}")
  public CareerCourseResponse update(
      @PathVariable UUID id, @RequestBody @Valid CareerCourseRequest req) {
    return careerCourseService.update(id, req);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    careerCourseService.delete(id);
  }
}
