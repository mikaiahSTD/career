package com.rmm.std.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class UiIT extends AbstractControllerIT {

  @Test
  @SneakyThrows
  void loginPage_renders_ok() {
    ResponseEntity<String> res = restTemplate.getForEntity(url("/ui/login"), String.class);

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertTrue(res.getBody().contains("Login"));
  }

  @Test
  @SneakyThrows
  void signupPage_renders_ok() {
    ResponseEntity<String> res = restTemplate.getForEntity(url("/ui/signup"), String.class);

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertTrue(res.getBody().contains("Sign Up"));
  }

  @Test
  @SneakyThrows
  void dashboard_renders_ok() {
    ResponseEntity<String> res = restTemplate.getForEntity(url("/ui"), String.class);

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertTrue(res.getBody().contains("Promotions"));
  }
}
