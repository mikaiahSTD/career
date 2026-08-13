package com.rmm.std.dto;

public record LoginResponse(String token, String type) {
  public static LoginResponse of(String token) {
    return new LoginResponse(token, "Bearer");
  }
}
