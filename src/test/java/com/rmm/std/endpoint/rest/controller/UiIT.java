package com.rmm.std.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rmm.std.constant.Role;
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
  void dashboard_withoutAuth_forbidden() {
    ResponseEntity<String> res = get("/ui", null);

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void dashboard_asAdmin_showsPromotions() {
    var admin = registerAndLogin(Role.ADMIN);
    createPromotion(admin.token());

    ResponseEntity<String> res = get("/ui", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertTrue(res.getBody().contains("Promotions / Cohorts"));
    assertTrue(res.getBody().contains("Start Year"));
    assertTrue(res.getBody().contains("Download Graduates (Excel)"));
  }

  @Test
  @SneakyThrows
  void dashboard_asTeacher_showsPromotions() {
    var teacher = registerAndLogin(Role.TEACHER);

    ResponseEntity<String> res = get("/ui", teacher.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertTrue(res.getBody().contains("Promotions / Cohorts"));
    assertTrue(res.getBody().contains("Start Year"));
  }

  @Test
  @SneakyThrows
  void dashboard_asStudent_hidesPromotions() {
    var student = registerAndLogin(Role.STUDENT);

    ResponseEntity<String> res = get("/ui", student.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertTrue(!res.getBody().contains("Promotions / Cohorts"));
    assertTrue(!res.getBody().contains("Download Graduates (Excel)"));
    assertTrue(res.getBody().contains("My Grades"));
  }
}
