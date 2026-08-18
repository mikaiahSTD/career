package com.rmm.std.endpoint.rest.controller;

import com.rmm.std.constant.Role;
import com.rmm.std.dto.DegreeResponse;
import com.rmm.std.exception.ForbiddenException;
import com.rmm.std.security.UserPrincipal;
import com.rmm.std.service.DegreeService;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class DegreeController {

  private final DegreeService degreeService;

  @GetMapping("/students/{studentId}/degrees")
  public DegreeResponse getDegree(
      @PathVariable UUID studentId, @AuthenticationPrincipal UserPrincipal principal) {
    if (principal.getUser().getRole() != Role.ADMIN
        && !principal.getUser().getId().equals(studentId)) {
      throw new ForbiddenException("You can only view your own degree");
    }
    return degreeService.getDegree(studentId);
  }
}
