package com.rmm.std.domain;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

  private UUID id;
  private String ref;
  private String firstname;
  private String lastname;
  private String email;
  private Role role;
}
