package com.rmm.std.dto;

import com.rmm.std.constant.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

  @NotBlank
  @Size(min = 8, max = 72, message = "password must be between 8 and 72 characters")
  @Pattern(
      regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
      message =
          "password must contain at least one uppercase letter, one lowercase letter and one digit")
  private String password;

  @NotNull private Role role;
}
