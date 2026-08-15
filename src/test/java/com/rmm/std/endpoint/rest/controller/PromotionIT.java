package com.rmm.std.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.rmm.std.constant.Role;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class PromotionIT extends AbstractControllerIT {

  @Test
  @SneakyThrows
  void createPromotion_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);

    ResponseEntity<String> res =
        post(
            "/promotions",
            json("{\"label\":\"P" + uuid() + "\",\"startYear\":2024}"),
            admin.token());

    assertEquals(HttpStatus.CREATED, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.has("id"));
    assertEquals(2024, body.get("startYear").asInt());
  }

  @Test
  @SneakyThrows
  void createPromotion_asStudent_forbidden() {
    var student = registerAndLogin(Role.STUDENT);

    ResponseEntity<String> res =
        post(
            "/promotions",
            json("{\"label\":\"P" + uuid() + "\",\"startYear\":2024}"),
            student.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createPromotion_blankLabel_badRequest() {
    var admin = registerAndLogin(Role.ADMIN);

    ResponseEntity<String> res =
        post("/promotions", json("{\"label\":\"\",\"startYear\":2024}"), admin.token());

    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.get("details").has("label"));
  }

  @Test
  @SneakyThrows
  void listPromotions_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    createPromotion(admin.token());

    ResponseEntity<String> res = get("/promotions?page=0&size=20", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.has("content"));
    assertTrue(body.get("content").isArray());
  }

  @Test
  @SneakyThrows
  void getPromotion_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String id = createPromotion(admin.token());

    ResponseEntity<String> res = get("/promotions/" + id, admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(id, objectMapper.readTree(res.getBody()).get("id").asText());
  }

  @Test
  @SneakyThrows
  void getPromotion_unknownId_notFound() {
    var admin = registerAndLogin(Role.ADMIN);

    ResponseEntity<String> res = get("/promotions/" + UUID.randomUUID(), admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void updatePromotion_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String id = createPromotion(admin.token());

    ResponseEntity<String> res =
        put(
            "/promotions/" + id,
            json("{\"label\":\"P" + uuid() + "\",\"startYear\":2025}"),
            admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertEquals(2025, body.get("startYear").asInt());
  }

  @Test
  @SneakyThrows
  void deletePromotion_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String id = createPromotion(admin.token());

    ResponseEntity<String> res = delete("/promotions/" + id, admin.token());

    assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void getGraduates_empty_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String id = createPromotion(admin.token());

    ResponseEntity<String> res = get("/promotions/" + id + "/graduates", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.isArray());
  }

  @Test
  @SneakyThrows
  void getGraduates_unknownId_notFound() {
    var admin = registerAndLogin(Role.ADMIN);

    ResponseEntity<String> res =
        get("/promotions/" + UUID.randomUUID() + "/graduates", admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void getGraduates_asStudent_forbidden() {
    var student = registerAndLogin(Role.STUDENT);
    var admin = registerAndLogin(Role.ADMIN);
    String id = createPromotion(admin.token());

    ResponseEntity<String> res = get("/promotions/" + id + "/graduates", student.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }
}
