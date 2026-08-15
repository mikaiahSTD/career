package com.rmm.std.endpoint.rest.controller;

import com.rmm.std.dto.PageResponse;
import com.rmm.std.dto.SemesterRequest;
import com.rmm.std.dto.SemesterResponse;
import com.rmm.std.service.SemesterService;
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
@RequestMapping("/semesters")
public class SemesterController {

  private final SemesterService semesterService;

  @GetMapping
  public PageResponse<SemesterResponse> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) UUID promotionId) {
    return semesterService.list(promotionId, PageRequest.of(page, Math.min(size, 200)));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public SemesterResponse create(@RequestBody @Valid SemesterRequest req) {
    return semesterService.create(req);
  }

  @GetMapping("/{id}")
  public SemesterResponse get(@PathVariable UUID id) {
    return semesterService.get(id);
  }

  @PutMapping("/{id}")
  public SemesterResponse update(@PathVariable UUID id, @RequestBody @Valid SemesterRequest req) {
    return semesterService.update(id, req);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    semesterService.delete(id);
  }
}
