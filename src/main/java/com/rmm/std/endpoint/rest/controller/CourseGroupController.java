package com.rmm.std.endpoint.rest.controller;

import com.rmm.std.dto.CourseGroupRequest;
import com.rmm.std.dto.CourseGroupResponse;
import com.rmm.std.dto.PageResponse;
import com.rmm.std.service.CourseGroupService;
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
@RequestMapping("/course-groups")
public class CourseGroupController {

  private final CourseGroupService courseGroupService;

  @GetMapping
  public PageResponse<CourseGroupResponse> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) UUID courseId,
      @RequestParam(required = false) UUID teacherId,
      @RequestParam(required = false) UUID groupId) {
    return courseGroupService.list(
        courseId, teacherId, groupId, PageRequest.of(page, Math.min(size, 200)));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CourseGroupResponse create(@RequestBody @Valid CourseGroupRequest req) {
    return courseGroupService.create(req);
  }

  @GetMapping("/{id}")
  public CourseGroupResponse get(@PathVariable UUID id) {
    return courseGroupService.get(id);
  }

  @PutMapping("/{id}")
  public CourseGroupResponse update(
      @PathVariable UUID id, @RequestBody @Valid CourseGroupRequest req) {
    return courseGroupService.update(id, req);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    courseGroupService.delete(id);
  }
}
