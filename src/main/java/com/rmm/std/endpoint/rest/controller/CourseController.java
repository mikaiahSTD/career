package com.rmm.std.endpoint.rest.controller;

import com.rmm.std.dto.CourseRequest;
import com.rmm.std.dto.CourseResponse;
import com.rmm.std.dto.PageResponse;
import com.rmm.std.service.CourseService;
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
@RequestMapping("/courses")
public class CourseController {

  private final CourseService courseService;

  @GetMapping
  public PageResponse<CourseResponse> list(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return courseService.list(PageRequest.of(page, Math.min(size, 200)));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CourseResponse create(@RequestBody @Valid CourseRequest req) {
    return courseService.create(req);
  }

  @GetMapping("/{id}")
  public CourseResponse get(@PathVariable UUID id) {
    return courseService.get(id);
  }

  @PutMapping("/{id}")
  public CourseResponse update(@PathVariable UUID id, @RequestBody @Valid CourseRequest req) {
    return courseService.update(id, req);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    courseService.delete(id);
  }
}
