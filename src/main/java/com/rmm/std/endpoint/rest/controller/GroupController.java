package com.rmm.std.endpoint.rest.controller;

import com.rmm.std.dto.GroupRequest;
import com.rmm.std.dto.GroupResponse;
import com.rmm.std.dto.PageResponse;
import com.rmm.std.service.GroupService;
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
@RequestMapping("/groups")
public class GroupController {

  private final GroupService groupService;

  @GetMapping
  public PageResponse<GroupResponse> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) UUID promotionId,
      @RequestParam(required = false) UUID careerId) {
    return groupService.list(promotionId, careerId, PageRequest.of(page, Math.min(size, 200)));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public GroupResponse create(@RequestBody @Valid GroupRequest req) {
    return groupService.create(req);
  }

  @GetMapping("/{id}")
  public GroupResponse get(@PathVariable UUID id) {
    return groupService.get(id);
  }

  @PutMapping("/{id}")
  public GroupResponse update(@PathVariable UUID id, @RequestBody @Valid GroupRequest req) {
    return groupService.update(id, req);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    groupService.delete(id);
  }
}
