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

class UserGroupIT extends AbstractControllerIT {

  private record UserGroupContext(String userId, String groupId, String token) {}

  private UserGroupContext setup(String adminToken) throws Exception {
    var user = registerAndLogin(Role.STUDENT);
    String promotionId = createPromotion(adminToken);
    String careerId = createCareer(adminToken);
    String groupId = createGroup(adminToken, promotionId, careerId);
    return new UserGroupContext(user.id().toString(), groupId, user.token());
  }

  @Test
  @SneakyThrows
  void createUserGroup_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());

    ResponseEntity<String> res =
        post(
            "/user-groups",
            json("{\"userId\":\"" + ctx.userId() + "\",\"groupId\":\"" + ctx.groupId() + "\"}"),
            admin.token());

    assertEquals(HttpStatus.CREATED, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.has("id"));
    assertEquals(ctx.userId(), body.get("userId").asText());
    assertEquals(ctx.groupId(), body.get("groupId").asText());
    assertNotNull(body.get("startDate"));
  }

  @Test
  @SneakyThrows
  void createUserGroup_unknownUser_notFound() {
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());
    String careerId = createCareer(admin.token());
    String groupId = createGroup(admin.token(), promotionId, careerId);

    ResponseEntity<String> res =
        post(
            "/user-groups",
            json("{\"userId\":\"" + UUID.randomUUID() + "\",\"groupId\":\"" + groupId + "\"}"),
            admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createUserGroup_unknownGroup_notFound() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());

    ResponseEntity<String> res =
        post(
            "/user-groups",
            json("{\"userId\":\"" + ctx.userId() + "\",\"groupId\":\"" + UUID.randomUUID() + "\"}"),
            admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createUserGroup_closesPreviousOpenMembership() {
    var admin = registerAndLogin(Role.ADMIN);
    var user = registerAndLogin(Role.STUDENT);
    String promotionId = createPromotion(admin.token());
    String careerId = createCareer(admin.token());
    String group1 = createGroup(admin.token(), promotionId, careerId);
    String group2 = createGroup(admin.token(), promotionId, careerId);

    String firstId = createUserGroup(admin.token(), user.id().toString(), group1);
    String secondId = createUserGroup(admin.token(), user.id().toString(), group2);

    JsonNode second =
        objectMapper.readTree(get("/user-groups/" + secondId, admin.token()).getBody());
    assertEquals(user.id().toString(), second.get("userId").asText());
    JsonNode first = objectMapper.readTree(get("/user-groups/" + firstId, admin.token()).getBody());
    assertNotNull(first.get("endDate"));
  }

  @Test
  @SneakyThrows
  void createUserGroup_asStudent_forbidden() {
    var student = registerAndLogin(Role.STUDENT);
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());

    ResponseEntity<String> res =
        post(
            "/user-groups",
            json("{\"userId\":\"" + ctx.userId() + "\",\"groupId\":\"" + ctx.groupId() + "\"}"),
            student.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void listUserGroups_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    createUserGroup(admin.token(), ctx.userId(), ctx.groupId());

    ResponseEntity<String> res = get("/user-groups?page=0&size=20", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.has("content"));
    assertTrue(body.get("content").isArray());
  }

  @Test
  @SneakyThrows
  void listUserGroups_asTeacher_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    createUserGroup(admin.token(), ctx.userId(), ctx.groupId());
    var teacher = registerAndLogin(Role.TEACHER);

    ResponseEntity<String> res = get("/user-groups?page=0&size=20", teacher.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void listUserGroups_asStudent_forbidden() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    createUserGroup(admin.token(), ctx.userId(), ctx.groupId());
    var student = registerAndLogin(Role.STUDENT);

    ResponseEntity<String> res = get("/user-groups?page=0&size=20", student.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void listUserGroups_filterByCurrent_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    createUserGroup(admin.token(), ctx.userId(), ctx.groupId());

    ResponseEntity<String> res = get("/user-groups?current=true&page=0&size=20", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode content = objectMapper.readTree(res.getBody()).get("content");
    for (JsonNode node : content) {
      assertTrue(node.has("endDate") ? node.get("endDate").isNull() : node.get("endDate") == null);
    }
  }

  @Test
  @SneakyThrows
  void getUserGroup_ownMembership_asStudent_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    String id = createUserGroup(admin.token(), ctx.userId(), ctx.groupId());

    ResponseEntity<String> res = get("/user-groups/" + id, ctx.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void getUserGroup_otherMembership_asStudent_forbidden() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    String id = createUserGroup(admin.token(), ctx.userId(), ctx.groupId());
    var other = registerAndLogin(Role.STUDENT);

    ResponseEntity<String> res = get("/user-groups/" + id, other.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void getUserGroup_unknownId_notFound() {
    var admin = registerAndLogin(Role.ADMIN);

    ResponseEntity<String> res = get("/user-groups/" + UUID.randomUUID(), admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void updateUserGroup_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    String id = createUserGroup(admin.token(), ctx.userId(), ctx.groupId());

    ResponseEntity<String> res =
        put(
            "/user-groups/" + id,
            json(
                "{\"userId\":\""
                    + ctx.userId()
                    + "\",\"groupId\":\""
                    + ctx.groupId()
                    + "\",\"startDate\":\"2024-01-01T00:00:00Z\"}"),
            admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(ctx.userId(), objectMapper.readTree(res.getBody()).get("userId").asText());
  }

  @Test
  @SneakyThrows
  void deleteUserGroup_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    String id = createUserGroup(admin.token(), ctx.userId(), ctx.groupId());

    ResponseEntity<String> res = delete("/user-groups/" + id, admin.token());

    assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
  }
}
