package com.rmm.std.endpoint.rest.controller;

import com.rmm.std.dto.CareerRequest;
import com.rmm.std.dto.CareerResponse;
import com.rmm.std.dto.PageResponse;
import com.rmm.std.service.CareerService;
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
@RequestMapping("/careers")
public class CareerController {

  private final CareerService careerService;

  @GetMapping
  public PageResponse<CareerResponse> list(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return careerService.list(PageRequest.of(page, Math.min(size, 200)));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CareerResponse create(@RequestBody @Valid CareerRequest req) {
    return careerService.create(req);
  }

  @GetMapping("/{id}")
  public CareerResponse get(@PathVariable UUID id) {
    return careerService.get(id);
  }

  @PutMapping("/{id}")
  public CareerResponse update(@PathVariable UUID id, @RequestBody @Valid CareerRequest req) {
    return careerService.update(id, req);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    careerService.delete(id);
  }
}
