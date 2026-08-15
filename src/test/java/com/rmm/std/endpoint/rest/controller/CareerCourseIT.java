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

class CareerCourseIT extends AbstractControllerIT {

  @Test
  @SneakyThrows
  void createCareerCourse_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String careerId = createCareer(admin.token());
    String courseId = createCourse(admin.token());

    ResponseEntity<String> res =
        post(
            "/career-courses",
            json(
                "{\"careerId\":\""
                    + careerId
                    + "\",\"courseId\":\""
                    + courseId
                    + "\",\"semesterNumber\":1}"),
            admin.token());

    assertEquals(HttpStatus.CREATED, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.has("id"));
    assertEquals(careerId, body.get("careerId").asText());
    assertEquals(courseId, body.get("courseId").asText());
    assertEquals(1, body.get("semesterNumber").asInt());
  }

  @Test
  @SneakyThrows
  void createCareerCourse_unknownCareer_notFound() {
    var admin = registerAndLogin(Role.ADMIN);
    String courseId = createCourse(admin.token());

    ResponseEntity<String> res =
        post(
            "/career-courses",
            json(
                "{\"careerId\":\""
                    + UUID.randomUUID()
                    + "\",\"courseId\":\""
                    + courseId
                    + "\",\"semesterNumber\":1}"),
            admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createCareerCourse_unknownCourse_notFound() {
    var admin = registerAndLogin(Role.ADMIN);
    String careerId = createCareer(admin.token());

    ResponseEntity<String> res =
        post(
            "/career-courses",
            json(
                "{\"careerId\":\""
                    + careerId
                    + "\",\"courseId\":\""
                    + UUID.randomUUID()
                    + "\",\"semesterNumber\":1}"),
            admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createCareerCourse_duplicate_conflict() {
    var admin = registerAndLogin(Role.ADMIN);
    String careerId = createCareer(admin.token());
    String courseId = createCourse(admin.token());
    post(
        "/career-courses",
        json(
            "{\"careerId\":\""
                + careerId
                + "\",\"courseId\":\""
                + courseId
                + "\",\"semesterNumber\":1}"),
        admin.token());

    ResponseEntity<String> res =
        post(
            "/career-courses",
            json(
                "{\"careerId\":\""
                    + careerId
                    + "\",\"courseId\":\""
                    + courseId
                    + "\",\"semesterNumber\":1}"),
            admin.token());

    assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createCareerCourse_asStudent_forbidden() {
    var student = registerAndLogin(Role.STUDENT);
    var admin = registerAndLogin(Role.ADMIN);
    String careerId = createCareer(admin.token());
    String courseId = createCourse(admin.token());

    ResponseEntity<String> res =
        post(
            "/career-courses",
            json(
                "{\"careerId\":\""
                    + careerId
                    + "\",\"courseId\":\""
                    + courseId
                    + "\",\"semesterNumber\":1}"),
            student.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void listCareerCourses_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String careerId = createCareer(admin.token());
    String courseId = createCourse(admin.token());
    createCareerCourse(admin.token(), careerId, courseId, 1);

    ResponseEntity<String> res = get("/career-courses?page=0&size=20", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.has("content"));
    assertTrue(body.get("content").isArray());
  }

  @Test
  @SneakyThrows
  void listCareerCourses_filterByCareer_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String careerId = createCareer(admin.token());
    String courseId = createCourse(admin.token());
    createCareerCourse(admin.token(), careerId, courseId, 1);

    ResponseEntity<String> res =
        get("/career-courses?careerId=" + careerId + "&page=0&size=20", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode content = objectMapper.readTree(res.getBody()).get("content");
    for (JsonNode node : content) {
      assertEquals(careerId, node.get("careerId").asText());
    }
  }

  @Test
  @SneakyThrows
  void getCareerCourse_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String careerId = createCareer(admin.token());
    String courseId = createCourse(admin.token());
    String id = createCareerCourse(admin.token(), careerId, courseId, 1);

    ResponseEntity<String> res = get("/career-courses/" + id, admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(id, objectMapper.readTree(res.getBody()).get("id").asText());
  }

  @Test
  @SneakyThrows
  void getCareerCourse_unknownId_notFound() {
    var admin = registerAndLogin(Role.ADMIN);

    ResponseEntity<String> res = get("/career-courses/" + UUID.randomUUID(), admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void updateCareerCourse_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String careerId = createCareer(admin.token());
    String courseId = createCourse(admin.token());
    String id = createCareerCourse(admin.token(), careerId, courseId, 1);

    ResponseEntity<String> res =
        put(
            "/career-courses/" + id,
            json(
                "{\"careerId\":\""
                    + careerId
                    + "\",\"courseId\":\""
                    + courseId
                    + "\",\"semesterNumber\":2}"),
            admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(2, objectMapper.readTree(res.getBody()).get("semesterNumber").asInt());
  }

  @Test
  @SneakyThrows
  void deleteCareerCourse_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String careerId = createCareer(admin.token());
    String courseId = createCourse(admin.token());
    String id = createCareerCourse(admin.token(), careerId, courseId, 1);

    ResponseEntity<String> res = delete("/career-courses/" + id, admin.token());

    assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
  }
}
