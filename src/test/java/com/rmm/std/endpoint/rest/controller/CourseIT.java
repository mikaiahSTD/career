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

class CourseIT extends AbstractControllerIT {

  @Test
  @SneakyThrows
  void createCourse_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String ref = "CRS-" + uuid();

    ResponseEntity<String> res =
        post(
            "/courses",
            json("{\"ref\":\"" + ref + "\",\"title\":\"Algo\",\"credits\":5}"),
            admin.token());

    assertEquals(HttpStatus.CREATED, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.has("id"));
    assertEquals(ref, body.get("ref").asText());
    assertEquals("Algo", body.get("title").asText());
    assertEquals(5, body.get("credits").asInt());
  }

  @Test
  @SneakyThrows
  void createCourse_duplicateRef_conflict() {
    var admin = registerAndLogin(Role.ADMIN);
    String ref = "CRS-" + uuid();
    post("/courses", json("{\"ref\":\"" + ref + "\"}"), admin.token());

    ResponseEntity<String> res = post("/courses", json("{\"ref\":\"" + ref + "\"}"), admin.token());

    assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
    assertEquals("CONFLICT", objectMapper.readTree(res.getBody()).get("error").asText());
  }

  @Test
  @SneakyThrows
  void createCourse_blankRef_badRequest() {
    var admin = registerAndLogin(Role.ADMIN);

    ResponseEntity<String> res = post("/courses", json("{\"ref\":\"\"}"), admin.token());

    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.get("details").has("ref"));
  }

  @Test
  @SneakyThrows
  void createCourse_asStudent_forbidden() {
    var student = registerAndLogin(Role.STUDENT);

    ResponseEntity<String> res =
        post("/courses", json("{\"ref\":\"CRS-" + uuid() + "\"}"), student.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void listCourses_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    createCourse(admin.token());

    ResponseEntity<String> res = get("/courses?page=0&size=20", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.has("content"));
    assertTrue(body.get("content").isArray());
  }

  @Test
  @SneakyThrows
  void getCourse_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String id = createCourse(admin.token());

    ResponseEntity<String> res = get("/courses/" + id, admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(id, objectMapper.readTree(res.getBody()).get("id").asText());
  }

  @Test
  @SneakyThrows
  void getCourse_unknownId_notFound() {
    var admin = registerAndLogin(Role.ADMIN);

    ResponseEntity<String> res = get("/courses/" + UUID.randomUUID(), admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void updateCourse_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String id = createCourse(admin.token());
    String newRef = "CRS-" + uuid();

    ResponseEntity<String> res =
        put(
            "/courses/" + id,
            json("{\"ref\":\"" + newRef + "\",\"title\":\"BDD\",\"credits\":4}"),
            admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertEquals(newRef, body.get("ref").asText());
    assertEquals("BDD", body.get("title").asText());
    assertEquals(4, body.get("credits").asInt());
  }

  @Test
  @SneakyThrows
  void updateCourse_duplicateRef_conflict() {
    var admin = registerAndLogin(Role.ADMIN);
    String firstId = createCourse(admin.token());
    String secondId = createCourse(admin.token());
    String firstRef =
        objectMapper
            .readTree(get("/courses/" + firstId, admin.token()).getBody())
            .get("ref")
            .asText();

    ResponseEntity<String> res =
        put("/courses/" + secondId, json("{\"ref\":\"" + firstRef + "\"}"), admin.token());

    assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void deleteCourse_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String id = createCourse(admin.token());

    ResponseEntity<String> res = delete("/courses/" + id, admin.token());

    assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
  }
}
