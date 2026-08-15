package com.rmm.std.endpoint.rest.controller;

import com.rmm.std.dto.GradeCorrectionRequest;
import com.rmm.std.dto.GradeHistoryResponse;
import com.rmm.std.dto.GradeRequest;
import com.rmm.std.dto.GradeResponse;
import com.rmm.std.dto.PageResponse;
import com.rmm.std.security.UserPrincipal;
import com.rmm.std.service.GradeService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/grades")
public class GradeController {

  private final GradeService gradeService;

  @GetMapping
  public PageResponse<GradeResponse> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) UUID examId,
      @RequestParam(required = false) UUID studentId,
      @AuthenticationPrincipal UserPrincipal principal) {
    return gradeService.list(
        principal.getUser(), examId, studentId, PageRequest.of(page, Math.min(size, 200)));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public GradeResponse create(
      @RequestBody @Valid GradeRequest req, @AuthenticationPrincipal UserPrincipal principal) {
    return gradeService.create(principal.getUser(), req);
  }

  @GetMapping("/{id}")
  public GradeResponse get(
      @PathVariable UUID id, @AuthenticationPrincipal UserPrincipal principal) {
    return gradeService.get(principal.getUser(), id);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    gradeService.delete(id);
  }

  @PatchMapping("/{id}/correct")
  public GradeResponse correct(
      @PathVariable UUID id,
      @RequestBody @Valid GradeCorrectionRequest req,
      @AuthenticationPrincipal UserPrincipal principal) {
    return gradeService.correct(principal.getUser(), id, req);
  }

  @GetMapping("/{id}/history")
  public List<GradeHistoryResponse> history(
      @PathVariable UUID id, @AuthenticationPrincipal UserPrincipal principal) {
    return gradeService.history(principal.getUser(), id);
  }
}
