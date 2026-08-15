package com.rmm.std.endpoint.rest.controller;

import com.rmm.std.constant.Role;
import com.rmm.std.dto.GradeResponse;
import com.rmm.std.dto.PageResponse;
import com.rmm.std.dto.UserRequest;
import com.rmm.std.dto.UserResponse;
import com.rmm.std.exception.ForbiddenException;
import com.rmm.std.security.UserPrincipal;
import com.rmm.std.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/users")
public class UserController {

  private final UserService userService;

  @GetMapping
  public PageResponse<UserResponse> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) Role role) {
    return userService.list(role, PageRequest.of(page, Math.min(size, 200)));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse create(@RequestBody @Valid UserRequest req) {
    return userService.createUser(req);
  }

  @GetMapping("/me/grades")
  public List<GradeResponse> myGrades(
      @RequestParam(required = false) UUID semesterId,
      @AuthenticationPrincipal UserPrincipal principal) {
    return userService.getMyGrades(principal.getUser().getId(), semesterId);
  }

  @GetMapping("/{id}")
  public UserResponse get(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal principal) {
    UserPrincipal requester = principal;
    if (requester.getUser().getRole() != Role.ADMIN && !requester.getUser().getId().equals(id)) {
      throw new ForbiddenException("You can only fetch your own profile");
    }
    return userService.get(id);
  }

  @PutMapping("/{id}")
  public UserResponse update(@PathVariable UUID id, @RequestBody @Valid UserRequest req) {
    return userService.update(id, req);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    userService.delete(id);
  }
}
