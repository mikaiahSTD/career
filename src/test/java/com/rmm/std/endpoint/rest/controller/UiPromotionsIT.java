package com.rmm.std.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rmm.std.constant.Role;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class UiPromotionsIT extends AbstractControllerIT {

  @Test
  @SneakyThrows
  void uiPromotions_asAdmin_returnsView() {
    var admin = registerAndLogin(Role.ADMIN);
    createPromotion(admin.token());

    ResponseEntity<String> res = get("/ui/promotions", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertTrue(res.getBody().contains("Promotions"));
  }

  @Test
  @SneakyThrows
  void uiPromotions_asStudent_forbidden() {
    var student = registerAndLogin(Role.STUDENT);

    ResponseEntity<String> res = get("/ui/promotions", student.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void uiPromotions_withoutToken_forbidden() {
    ResponseEntity<String> res = get("/ui/promotions", null);

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }
}
