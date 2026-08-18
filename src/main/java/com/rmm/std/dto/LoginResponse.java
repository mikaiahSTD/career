package com.rmm.std.dto;

import java.util.UUID;

public record LoginResponse(String token, String type, UUID userId, String role) {
  public static LoginResponse of(String token, UUID userId, String role) {
    return new LoginResponse(token, "Bearer", userId, role);
  }
}
