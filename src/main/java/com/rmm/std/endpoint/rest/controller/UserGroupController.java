package com.rmm.std.endpoint.rest.controller;

import com.rmm.std.dto.PageResponse;
import com.rmm.std.dto.UserGroupRequest;
import com.rmm.std.dto.UserGroupResponse;
import com.rmm.std.security.UserPrincipal;
import com.rmm.std.service.UserGroupService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/user-groups")
public class UserGroupController {

  private final UserGroupService userGroupService;

  @GetMapping
  public PageResponse<UserGroupResponse> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) UUID userId,
      @RequestParam(required = false) UUID groupId,
      @RequestParam(required = false) Boolean current) {
    return userGroupService.list(
        userId, groupId, current, PageRequest.of(page, Math.min(size, 200)));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UserGroupResponse create(@RequestBody @Valid UserGroupRequest req) {
    return userGroupService.create(req);
  }

  @GetMapping("/{id}")
  public UserGroupResponse get(
      @PathVariable UUID id, @AuthenticationPrincipal UserPrincipal principal) {
    userGroupService.assertCanRead(principal.getUser(), id);
    return userGroupService.get(id);
  }

  @PutMapping("/{id}")
  public UserGroupResponse update(@PathVariable UUID id, @RequestBody @Valid UserGroupRequest req) {
    return userGroupService.update(id, req);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    userGroupService.delete(id);
  }
}
