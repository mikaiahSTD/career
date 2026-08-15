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

class SemesterIT extends AbstractControllerIT {

  @Test
  @SneakyThrows
  void createSemester_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());

    ResponseEntity<String> res =
        post(
            "/semesters",
            json("{\"promotionId\":\"" + promotionId + "\",\"number\":1}"),
            admin.token());

    assertEquals(HttpStatus.CREATED, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.has("id"));
    assertEquals(promotionId, body.get("promotionId").asText());
    assertEquals(1, body.get("number").asInt());
  }

  @Test
  @SneakyThrows
  void createSemester_unknownPromotion_notFound() {
    var admin = registerAndLogin(Role.ADMIN);

    ResponseEntity<String> res =
        post(
            "/semesters",
            json("{\"promotionId\":\"" + UUID.randomUUID() + "\",\"number\":1}"),
            admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createSemester_asStudent_forbidden() {
    var student = registerAndLogin(Role.STUDENT);
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());

    ResponseEntity<String> res =
        post(
            "/semesters",
            json("{\"promotionId\":\"" + promotionId + "\",\"number\":1}"),
            student.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void listSemesters_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());
    createSemester(admin.token(), promotionId);

    ResponseEntity<String> res = get("/semesters?page=0&size=20", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.has("content"));
    assertTrue(body.get("content").isArray());
  }

  @Test
  @SneakyThrows
  void listSemesters_filterByPromotion_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());
    createSemester(admin.token(), promotionId);

    ResponseEntity<String> res =
        get("/semesters?promotionId=" + promotionId + "&page=0&size=20", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode content = objectMapper.readTree(res.getBody()).get("content");
    for (JsonNode node : content) {
      assertEquals(promotionId, node.get("promotionId").asText());
    }
  }

  @Test
  @SneakyThrows
  void getSemester_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());
    String id = createSemester(admin.token(), promotionId);

    ResponseEntity<String> res = get("/semesters/" + id, admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(id, objectMapper.readTree(res.getBody()).get("id").asText());
  }

  @Test
  @SneakyThrows
  void getSemester_unknownId_notFound() {
    var admin = registerAndLogin(Role.ADMIN);

    ResponseEntity<String> res = get("/semesters/" + UUID.randomUUID(), admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void updateSemester_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());
    String id = createSemester(admin.token(), promotionId);

    ResponseEntity<String> res =
        put(
            "/semesters/" + id,
            json("{\"promotionId\":\"" + promotionId + "\",\"number\":2}"),
            admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(2, objectMapper.readTree(res.getBody()).get("number").asInt());
  }

  @Test
  @SneakyThrows
  void updateSemester_unknownPromotion_notFound() {
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());
    String id = createSemester(admin.token(), promotionId);

    ResponseEntity<String> res =
        put(
            "/semesters/" + id,
            json("{\"promotionId\":\"" + UUID.randomUUID() + "\",\"number\":2}"),
            admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void deleteSemester_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());
    String id = createSemester(admin.token(), promotionId);

    ResponseEntity<String> res = delete("/semesters/" + id, admin.token());

    assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
  }
}
