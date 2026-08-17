package com.rmm.std.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.rmm.std.constant.Role;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class UserPromotionIT extends AbstractControllerIT {

  private record UserPromotionContext(String userId, String promotionId) {}

  private UserPromotionContext setup(String adminToken) throws Exception {
    var user = registerAndLogin(Role.STUDENT);
    String promotionId = createPromotion(adminToken);
    return new UserPromotionContext(user.id().toString(), promotionId);
  }

  @Test
  @SneakyThrows
  void createUserPromotion_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());

    ResponseEntity<String> res =
        post(
            "/user-promotions",
            json(
                "{\"userId\":\""
                    + ctx.userId()
                    + "\",\"promotionId\":\""
                    + ctx.promotionId()
                    + "\"}"),
            admin.token());

    assertEquals(HttpStatus.CREATED, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.has("id"));
    assertEquals(ctx.userId(), body.get("userId").asText());
    assertEquals(ctx.promotionId(), body.get("promotionId").asText());
    assertEquals("IN_PROGRESS", body.get("status").asText());
    assertNotNull(body.get("startDate"));
  }

  @Test
  @SneakyThrows
  void createUserPromotion_graduatedStatus_setsDate() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());

    ResponseEntity<String> res =
        post(
            "/user-promotions",
            json(
                "{\"userId\":\""
                    + ctx.userId()
                    + "\",\"promotionId\":\""
                    + ctx.promotionId()
                    + "\",\"status\":\"GRADUATED\"}"),
            admin.token());

    assertEquals(HttpStatus.CREATED, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertEquals("GRADUATED", body.get("status").asText());
    assertNotNull(body.get("graduationDate"));
  }

  @Test
  @SneakyThrows
  void createUserPromotion_unknownUser_notFound() {
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());

    ResponseEntity<String> res =
        post(
            "/user-promotions",
            json(
                "{\"userId\":\""
                    + UUID.randomUUID()
                    + "\",\"promotionId\":\""
                    + promotionId
                    + "\"}"),
            admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createUserPromotion_unknownPromotion_notFound() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());

    ResponseEntity<String> res =
        post(
            "/user-promotions",
            json(
                "{\"userId\":\""
                    + ctx.userId()
                    + "\",\"promotionId\":\""
                    + UUID.randomUUID()
                    + "\"}"),
            admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createUserPromotion_duplicate_conflict() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    String body =
        json("{\"userId\":\"" + ctx.userId() + "\",\"promotionId\":\"" + ctx.promotionId() + "\"}");
    post("/user-promotions", body, admin.token());

    ResponseEntity<String> res = post("/user-promotions", body, admin.token());

    assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createUserPromotion_asStudent_forbidden() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    var student = registerAndLogin(Role.STUDENT);

    ResponseEntity<String> res =
        post(
            "/user-promotions",
            json(
                "{\"userId\":\""
                    + ctx.userId()
                    + "\",\"promotionId\":\""
                    + ctx.promotionId()
                    + "\"}"),
            student.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void listUserPromotions_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    createUserPromotion(admin.token(), ctx.userId(), ctx.promotionId());

    ResponseEntity<String> res = get("/user-promotions?page=0&size=20", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.has("content"));
    assertTrue(body.get("content").isArray());
  }

  @Test
  @SneakyThrows
  void listUserPromotions_filterByStatus_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    createUserPromotion(admin.token(), ctx.userId(), ctx.promotionId());

    ResponseEntity<String> res =
        get("/user-promotions?status=IN_PROGRESS&page=0&size=20", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode content = objectMapper.readTree(res.getBody()).get("content");
    for (JsonNode node : content) {
      assertEquals("IN_PROGRESS", node.get("status").asText());
    }
  }

  @Test
  @SneakyThrows
  void getUserPromotion_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    String id = createUserPromotion(admin.token(), ctx.userId(), ctx.promotionId());

    ResponseEntity<String> res = get("/user-promotions/" + id, admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(id, objectMapper.readTree(res.getBody()).get("id").asText());
  }

  @Test
  @SneakyThrows
  void getUserPromotion_unknownId_notFound() {
    var admin = registerAndLogin(Role.ADMIN);

    ResponseEntity<String> res = get("/user-promotions/" + UUID.randomUUID(), admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void updateUserPromotion_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    String id = createUserPromotion(admin.token(), ctx.userId(), ctx.promotionId());

    ResponseEntity<String> res =
        put(
            "/user-promotions/" + id,
            json(
                "{\"userId\":\""
                    + ctx.userId()
                    + "\",\"promotionId\":\""
                    + ctx.promotionId()
                    + "\",\"status\":\"GRADUATED\"}"),
            admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertEquals("GRADUATED", body.get("status").asText());
    assertNotNull(body.get("graduationDate"));
  }

  @Test
  @SneakyThrows
  void deleteUserPromotion_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    String id = createUserPromotion(admin.token(), ctx.userId(), ctx.promotionId());

    ResponseEntity<String> res = delete("/user-promotions/" + id, admin.token());

    assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void repeatYear_asAdmin_closesOldAndCreatesNewEnrollment() {
    var admin = registerAndLogin(Role.ADMIN);
    var student = registerAndLogin(Role.STUDENT);
    String promotion1 = createPromotion(admin.token());
    String promotion2 = createPromotion(admin.token());
    String oldId = createUserPromotion(admin.token(), student.id().toString(), promotion1);

    ResponseEntity<String> res =
        post(
            "/user-promotions/repeat",
            json(
                "{\"userId\":\""
                    + student.id()
                    + "\",\"fromPromotionId\":\""
                    + promotion1
                    + "\",\"toPromotionId\":\""
                    + promotion2
                    + "\"}"),
            admin.token());

    assertEquals(HttpStatus.CREATED, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertEquals(promotion2, body.get("promotionId").asText());
    assertEquals("IN_PROGRESS", body.get("status").asText());
    assertNotNull(body.get("startDate"));

    JsonNode oldEnrollment =
        objectMapper.readTree(get("/user-promotions/" + oldId, admin.token()).getBody());
    assertEquals("REPEATING", oldEnrollment.get("status").asText());
    assertNotNull(oldEnrollment.get("endDate"));
  }

  @Test
  @SneakyThrows
  void repeatYear_unknownUser_notFound() {
    var admin = registerAndLogin(Role.ADMIN);
    String promotion1 = createPromotion(admin.token());
    String promotion2 = createPromotion(admin.token());

    ResponseEntity<String> res =
        post(
            "/user-promotions/repeat",
            json(
                "{\"userId\":\""
                    + UUID.randomUUID()
                    + "\",\"fromPromotionId\":\""
                    + promotion1
                    + "\",\"toPromotionId\":\""
                    + promotion2
                    + "\"}"),
            admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void repeatYear_unknownFromPromotion_notFound() {
    var admin = registerAndLogin(Role.ADMIN);
    var student = registerAndLogin(Role.STUDENT);
    String promotion2 = createPromotion(admin.token());

    ResponseEntity<String> res =
        post(
            "/user-promotions/repeat",
            json(
                "{\"userId\":\""
                    + student.id()
                    + "\",\"fromPromotionId\":\""
                    + UUID.randomUUID()
                    + "\",\"toPromotionId\":\""
                    + promotion2
                    + "\"}"),
            admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void repeatYear_noPreviousEnrollment_notFound() {
    var admin = registerAndLogin(Role.ADMIN);
    var student = registerAndLogin(Role.STUDENT);
    String promotion1 = createPromotion(admin.token());
    String promotion2 = createPromotion(admin.token());

    ResponseEntity<String> res =
        post(
            "/user-promotions/repeat",
            json(
                "{\"userId\":\""
                    + student.id()
                    + "\",\"fromPromotionId\":\""
                    + promotion1
                    + "\",\"toPromotionId\":\""
                    + promotion2
                    + "\"}"),
            admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void repeatYear_alreadyEnrolledInTarget_conflict() {
    var admin = registerAndLogin(Role.ADMIN);
    var student = registerAndLogin(Role.STUDENT);
    String promotion1 = createPromotion(admin.token());
    String promotion2 = createPromotion(admin.token());
    createUserPromotion(admin.token(), student.id().toString(), promotion1);
    createUserPromotion(admin.token(), student.id().toString(), promotion2);

    ResponseEntity<String> res =
        post(
            "/user-promotions/repeat",
            json(
                "{\"userId\":\""
                    + student.id()
                    + "\",\"fromPromotionId\":\""
                    + promotion1
                    + "\",\"toPromotionId\":\""
                    + promotion2
                    + "\"}"),
            admin.token());

    assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void repeatYear_asStudent_forbidden() {
    var admin = registerAndLogin(Role.ADMIN);
    var student = registerAndLogin(Role.STUDENT);
    String promotion1 = createPromotion(admin.token());
    String promotion2 = createPromotion(admin.token());

    ResponseEntity<String> res =
        post(
            "/user-promotions/repeat",
            json(
                "{\"userId\":\""
                    + student.id()
                    + "\",\"fromPromotionId\":\""
                    + promotion1
                    + "\",\"toPromotionId\":\""
                    + promotion2
                    + "\"}"),
            student.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }
}
