package com.rmm.std.endpoint.rest.controller;

import com.rmm.std.dto.CourseTeacherRequest;
import com.rmm.std.dto.CourseTeacherResponse;
import com.rmm.std.dto.PageResponse;
import com.rmm.std.service.CourseTeacherService;
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
@RequestMapping("/course-teachers")
public class CourseTeacherController {

  private final CourseTeacherService courseTeacherService;

  @GetMapping
  public PageResponse<CourseTeacherResponse> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) UUID courseId,
      @RequestParam(required = false) UUID teacherId) {
    return courseTeacherService.list(
        courseId, teacherId, PageRequest.of(page, Math.min(size, 200)));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CourseTeacherResponse create(@RequestBody @Valid CourseTeacherRequest req) {
    return courseTeacherService.create(req);
  }

  @GetMapping("/{id}")
  public CourseTeacherResponse get(@PathVariable UUID id) {
    return courseTeacherService.get(id);
  }

  @PutMapping("/{id}")
  public CourseTeacherResponse update(
      @PathVariable UUID id, @RequestBody @Valid CourseTeacherRequest req) {
    return courseTeacherService.update(id, req);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    courseTeacherService.delete(id);
  }
}
