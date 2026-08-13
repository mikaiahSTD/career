package com.rmm.std.dto;

import com.rmm.std.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

  @NotBlank private String ref;

  private String firstname;

  private String lastname;

  @NotBlank @Email private String email;

  @NotNull private Role role;
}
