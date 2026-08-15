package com.rmm.std.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.rmm.std.constant.Role;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class UserIT extends AbstractControllerIT {

  @Test
  @SneakyThrows
  void createUser_asAdmin_returnsUser() {
    var admin = registerAndLogin(Role.ADMIN);
    var req = randomUser(Role.STUDENT);

    ResponseEntity<String> res = post("/users", json(req), admin.token());

    assertEquals(HttpStatus.CREATED, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.has("id"));
    assertEquals(req.getRef(), body.get("ref").asText());
    assertEquals(req.getEmail(), body.get("email").asText());
    assertEquals(req.getRole().name(), body.get("role").asText());
    assertFalse(body.has("password"));
  }

  @Test
  @SneakyThrows
  void createUser_duplicateEmail_conflict() {
    var admin = registerAndLogin(Role.ADMIN);
    var req = randomUser(Role.STUDENT);
    post("/users", json(req), admin.token());

    ResponseEntity<String> res = post("/users", json(req), admin.token());

    assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertEquals("CONFLICT", body.get("error").asText());
  }

  @Test
  @SneakyThrows
  void createUser_asStudent_forbidden() {
    var student = registerAndLogin(Role.STUDENT);

    ResponseEntity<String> res = post("/users", json(randomUser(Role.STUDENT)), student.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void listUsers_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    registerAndLogin(Role.STUDENT);

    ResponseEntity<String> res = get("/users?page=0&size=20", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.has("content"));
    assertTrue(body.get("content").isArray());
  }

  @Test
  @SneakyThrows
  void listUsers_filterByRole_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var teacher = registerAndLogin(Role.TEACHER);
    registerAndLogin(Role.STUDENT);

    ResponseEntity<String> res = get("/users?role=TEACHER&page=0&size=20", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode content = objectMapper.readTree(res.getBody()).get("content");
    for (JsonNode node : content) {
      assertEquals("TEACHER", node.get("role").asText());
    }
  }

  @Test
  @SneakyThrows
  void listUsers_asStudent_forbidden() {
    var student = registerAndLogin(Role.STUDENT);

    ResponseEntity<String> res = get("/users", student.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void getUser_ownProfile_asStudent_ok() {
    var student = registerAndLogin(Role.STUDENT);

    ResponseEntity<String> res = get("/users/" + student.id(), student.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertEquals(student.id().toString(), body.get("id").asText());
  }

  @Test
  @SneakyThrows
  void getUser_otherProfile_asStudent_forbidden() {
    var student = registerAndLogin(Role.STUDENT);
    var other = registerAndLogin(Role.STUDENT);

    ResponseEntity<String> res = get("/users/" + other.id(), student.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void getUser_otherProfile_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var student = registerAndLogin(Role.STUDENT);

    ResponseEntity<String> res = get("/users/" + student.id(), admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void getUser_unknownId_notFound() {
    var admin = registerAndLogin(Role.ADMIN);

    ResponseEntity<String> res = get("/users/" + UUID.randomUUID(), admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void updateUser_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var target = registerAndLogin(Role.STUDENT);
    var req = randomUser(Role.STUDENT);

    ResponseEntity<String> res = put("/users/" + target.id(), json(req), admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertEquals(req.getEmail(), body.get("email").asText());
    assertEquals(req.getRole().name(), body.get("role").asText());
  }

  @Test
  @SneakyThrows
  void updateUser_duplicateEmail_conflict() {
    var admin = registerAndLogin(Role.ADMIN);
    var first = registerAndLogin(Role.STUDENT);
    var target = registerAndLogin(Role.STUDENT);
    var req = randomUser(Role.STUDENT);
    req.setEmail(first.email());

    ResponseEntity<String> res = put("/users/" + target.id(), json(req), admin.token());

    assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void updateUser_asStudent_forbidden() {
    var student = registerAndLogin(Role.STUDENT);
    var other = registerAndLogin(Role.STUDENT);

    ResponseEntity<String> res =
        put("/users/" + other.id(), json(randomUser(Role.STUDENT)), student.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void deleteUser_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var target = registerAndLogin(Role.STUDENT);

    ResponseEntity<String> res = delete("/users/" + target.id(), admin.token());

    assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void deleteUser_asStudent_forbidden() {
    var student = registerAndLogin(Role.STUDENT);
    var other = registerAndLogin(Role.STUDENT);

    ResponseEntity<String> res = delete("/users/" + other.id(), student.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void myGrades_asStudent_noSemester_emptyList() {
    var student = registerAndLogin(Role.STUDENT);

    ResponseEntity<String> res = get("/users/me/grades", student.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.isArray());
  }

  @Test
  @SneakyThrows
  void myGrades_asStudent_unknownSemester_notFound() {
    var student = registerAndLogin(Role.STUDENT);

    ResponseEntity<String> res =
        get("/users/me/grades?semesterId=" + UUID.randomUUID(), student.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void myGrades_asTeacher_forbidden() {
    var teacher = registerAndLogin(Role.TEACHER);

    ResponseEntity<String> res = get("/users/me/grades", teacher.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void myGrades_withSemester_returnsGrades() {
    var admin = registerAndLogin(Role.ADMIN);
    String careerId = createCareer(admin.token());
    String courseId = createCourse(admin.token());
    createCareerCourse(admin.token(), careerId, courseId, 1);
    String promotionId = createPromotion(admin.token());
    String semesterId = createSemester(admin.token(), promotionId);
    String groupId = createGroup(admin.token(), promotionId, careerId);
    var student = registerAndLogin(Role.STUDENT);
    createUserGroup(admin.token(), student.id().toString(), groupId);
    var teacher = registerAndLogin(Role.TEACHER);
    createCourseTeacher(admin.token(), courseId, teacher.id().toString());
    String examId = createExam(admin.token(), courseId);
    createGrade(admin.token(), examId, student.id().toString());

    ResponseEntity<String> res = get("/users/me/grades?semesterId=" + semesterId, student.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.isArray());
    assertEquals(1, body.size());
  }
}
