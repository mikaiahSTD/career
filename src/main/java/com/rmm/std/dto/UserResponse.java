package com.rmm.std.dto;

import com.rmm.std.domain.Role;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

  private UUID id;
  private String ref;
  private String firstname;
  private String lastname;
  private String email;
  private Role role;
}
