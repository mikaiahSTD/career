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

class CareerIT extends AbstractControllerIT {

  @Test
  @SneakyThrows
  void createCareer_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);

    ResponseEntity<String> res =
        post("/careers", json("{\"title\":\"Info\",\"specialization\":\"EL\"}"), admin.token());

    assertEquals(HttpStatus.CREATED, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.has("id"));
    assertEquals("Info", body.get("title").asText());
    assertEquals("EL", body.get("specialization").asText());
  }

  @Test
  @SneakyThrows
  void createCareer_asStudent_forbidden() {
    var student = registerAndLogin(Role.STUDENT);

    ResponseEntity<String> res =
        post("/careers", json("{\"title\":\"Info\",\"specialization\":\"EL\"}"), student.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void listCareers_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    createCareer(admin.token());

    ResponseEntity<String> res = get("/careers?page=0&size=20", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.has("content"));
    assertTrue(body.get("content").isArray());
  }

  @Test
  @SneakyThrows
  void getCareer_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String id = createCareer(admin.token());

    ResponseEntity<String> res = get("/careers/" + id, admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(id, objectMapper.readTree(res.getBody()).get("id").asText());
  }

  @Test
  @SneakyThrows
  void getCareer_unknownId_notFound() {
    var admin = registerAndLogin(Role.ADMIN);

    ResponseEntity<String> res = get("/careers/" + UUID.randomUUID(), admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void updateCareer_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String id = createCareer(admin.token());

    ResponseEntity<String> res =
        put(
            "/careers/" + id,
            json("{\"title\":\"Telecom\",\"specialization\":\"TN\"}"),
            admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertEquals("Telecom", body.get("title").asText());
    assertEquals("TN", body.get("specialization").asText());
  }

  @Test
  @SneakyThrows
  void updateCareer_unknownId_notFound() {
    var admin = registerAndLogin(Role.ADMIN);

    ResponseEntity<String> res =
        put(
            "/careers/" + UUID.randomUUID(),
            json("{\"title\":\"X\",\"specialization\":\"EL\"}"),
            admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void deleteCareer_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String id = createCareer(admin.token());

    ResponseEntity<String> res = delete("/careers/" + id, admin.token());

    assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void deleteCareer_asStudent_forbidden() {
    var student = registerAndLogin(Role.STUDENT);
    var admin = registerAndLogin(Role.ADMIN);
    String id = createCareer(admin.token());

    ResponseEntity<String> res = delete("/careers/" + id, student.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }
}
