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

class GroupIT extends AbstractControllerIT {

  @Test
  @SneakyThrows
  void createGroup_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());
    String careerId = createCareer(admin.token());

    ResponseEntity<String> res =
        post(
            "/groups",
            json(
                "{\"ref\":\"G-"
                    + uuid()
                    + "\",\"promotionId\":\""
                    + promotionId
                    + "\",\"careerId\":\""
                    + careerId
                    + "\"}"),
            admin.token());

    assertEquals(HttpStatus.CREATED, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.has("id"));
    assertEquals(promotionId, body.get("promotionId").asText());
    assertEquals(careerId, body.get("careerId").asText());
  }

  @Test
  @SneakyThrows
  void createGroup_duplicateRef_conflict() {
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());
    String careerId = createCareer(admin.token());
    String ref = "G-" + uuid();
    post(
        "/groups",
        json(
            "{\"ref\":\""
                + ref
                + "\",\"promotionId\":\""
                + promotionId
                + "\",\"careerId\":\""
                + careerId
                + "\"}"),
        admin.token());

    ResponseEntity<String> res =
        post(
            "/groups",
            json(
                "{\"ref\":\""
                    + ref
                    + "\",\"promotionId\":\""
                    + promotionId
                    + "\",\"careerId\":\""
                    + careerId
                    + "\"}"),
            admin.token());

    assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
    assertEquals("CONFLICT", objectMapper.readTree(res.getBody()).get("error").asText());
  }

  @Test
  @SneakyThrows
  void createGroup_unknownPromotion_notFound() {
    var admin = registerAndLogin(Role.ADMIN);
    String careerId = createCareer(admin.token());

    ResponseEntity<String> res =
        post(
            "/groups",
            json(
                "{\"ref\":\"G-"
                    + uuid()
                    + "\",\"promotionId\":\""
                    + UUID.randomUUID()
                    + "\",\"careerId\":\""
                    + careerId
                    + "\"}"),
            admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createGroup_unknownCareer_notFound() {
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());

    ResponseEntity<String> res =
        post(
            "/groups",
            json(
                "{\"ref\":\"G-"
                    + uuid()
                    + "\",\"promotionId\":\""
                    + promotionId
                    + "\",\"careerId\":\""
                    + UUID.randomUUID()
                    + "\"}"),
            admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createGroup_asStudent_forbidden() {
    var student = registerAndLogin(Role.STUDENT);
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());
    String careerId = createCareer(admin.token());

    ResponseEntity<String> res =
        post(
            "/groups",
            json(
                "{\"ref\":\"G-"
                    + uuid()
                    + "\",\"promotionId\":\""
                    + promotionId
                    + "\",\"careerId\":\""
                    + careerId
                    + "\"}"),
            student.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void listGroups_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());
    String careerId = createCareer(admin.token());
    createGroup(admin.token(), promotionId, careerId);

    ResponseEntity<String> res = get("/groups?page=0&size=20", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.has("content"));
    assertTrue(body.get("content").isArray());
  }

  @Test
  @SneakyThrows
  void listGroups_filterByPromotion_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());
    String careerId = createCareer(admin.token());
    createGroup(admin.token(), promotionId, careerId);

    ResponseEntity<String> res =
        get("/groups?promotionId=" + promotionId + "&page=0&size=20", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode content = objectMapper.readTree(res.getBody()).get("content");
    for (JsonNode node : content) {
      assertEquals(promotionId, node.get("promotionId").asText());
    }
  }

  @Test
  @SneakyThrows
  void listGroups_filterByCareer_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());
    String careerId = createCareer(admin.token());
    createGroup(admin.token(), promotionId, careerId);

    ResponseEntity<String> res =
        get("/groups?careerId=" + careerId + "&page=0&size=20", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode content = objectMapper.readTree(res.getBody()).get("content");
    for (JsonNode node : content) {
      assertEquals(careerId, node.get("careerId").asText());
    }
  }

  @Test
  @SneakyThrows
  void getGroup_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());
    String careerId = createCareer(admin.token());
    String id = createGroup(admin.token(), promotionId, careerId);

    ResponseEntity<String> res = get("/groups/" + id, admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(id, objectMapper.readTree(res.getBody()).get("id").asText());
  }

  @Test
  @SneakyThrows
  void getGroup_unknownId_notFound() {
    var admin = registerAndLogin(Role.ADMIN);

    ResponseEntity<String> res = get("/groups/" + UUID.randomUUID(), admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void updateGroup_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());
    String careerId = createCareer(admin.token());
    String id = createGroup(admin.token(), promotionId, careerId);
    String newRef = "G-" + uuid();

    ResponseEntity<String> res =
        put(
            "/groups/" + id,
            json(
                "{\"ref\":\""
                    + newRef
                    + "\",\"promotionId\":\""
                    + promotionId
                    + "\",\"careerId\":\""
                    + careerId
                    + "\"}"),
            admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(newRef, objectMapper.readTree(res.getBody()).get("ref").asText());
  }

  @Test
  @SneakyThrows
  void updateGroup_duplicateRef_conflict() {
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());
    String careerId = createCareer(admin.token());
    String firstId = createGroup(admin.token(), promotionId, careerId);
    String secondId = createGroup(admin.token(), promotionId, careerId);
    String firstRef =
        objectMapper
            .readTree(get("/groups/" + firstId, admin.token()).getBody())
            .get("ref")
            .asText();

    ResponseEntity<String> res =
        put(
            "/groups/" + secondId,
            json(
                "{\"ref\":\""
                    + firstRef
                    + "\",\"promotionId\":\""
                    + promotionId
                    + "\",\"careerId\":\""
                    + careerId
                    + "\"}"),
            admin.token());

    assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void deleteGroup_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());
    String careerId = createCareer(admin.token());
    String id = createGroup(admin.token(), promotionId, careerId);

    ResponseEntity<String> res = delete("/groups/" + id, admin.token());

    assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
  }
}
