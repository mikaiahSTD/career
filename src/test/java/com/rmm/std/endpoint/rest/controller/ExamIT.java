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

class ExamIT extends AbstractControllerIT {

  @Test
  @SneakyThrows
  void createExam_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String courseId = createCourse(admin.token());

    ResponseEntity<String> res =
        post(
            "/exams",
            json("{\"courseId\":\"" + courseId + "\",\"title\":\"Midterm\",\"coefficient\":2.0}"),
            admin.token());

    assertEquals(HttpStatus.CREATED, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.has("id"));
    assertEquals(courseId, body.get("courseId").asText());
    assertEquals("Midterm", body.get("title").asText());
  }

  @Test
  @SneakyThrows
  void createExam_asTeacherOwningCourse_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String courseId = createCourse(admin.token());
    var teacher = registerAndLogin(Role.TEACHER);
    createCourseTeacher(admin.token(), courseId, teacher.id().toString());

    ResponseEntity<String> res =
        post(
            "/exams",
            json("{\"courseId\":\"" + courseId + "\",\"title\":\"Final\",\"coefficient\":1.5}"),
            teacher.token());

    assertEquals(HttpStatus.CREATED, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createExam_asTeacherNotOwningCourse_forbidden() {
    var admin = registerAndLogin(Role.ADMIN);
    String courseId = createCourse(admin.token());
    var teacher = registerAndLogin(Role.TEACHER);

    ResponseEntity<String> res =
        post(
            "/exams",
            json("{\"courseId\":\"" + courseId + "\",\"title\":\"Final\",\"coefficient\":1.5}"),
            teacher.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createExam_unknownCourse_notFound() {
    var admin = registerAndLogin(Role.ADMIN);

    ResponseEntity<String> res =
        post(
            "/exams",
            json("{\"courseId\":\"" + UUID.randomUUID() + "\",\"coefficient\":1.0}"),
            admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createExam_invalidCoefficient_badRequest() {
    var admin = registerAndLogin(Role.ADMIN);
    String courseId = createCourse(admin.token());

    ResponseEntity<String> res =
        post(
            "/exams",
            json("{\"courseId\":\"" + courseId + "\",\"coefficient\":15.0}"),
            admin.token());

    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.get("details").has("coefficient"));
  }

  @Test
  @SneakyThrows
  void createExam_asStudent_forbidden() {
    var student = registerAndLogin(Role.STUDENT);
    var admin = registerAndLogin(Role.ADMIN);
    String courseId = createCourse(admin.token());

    ResponseEntity<String> res =
        post(
            "/exams",
            json("{\"courseId\":\"" + courseId + "\",\"coefficient\":1.0}"),
            student.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void listExams_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String courseId = createCourse(admin.token());
    createExam(admin.token(), courseId);

    ResponseEntity<String> res = get("/exams?page=0&size=20", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.has("content"));
    assertTrue(body.get("content").isArray());
  }

  @Test
  @SneakyThrows
  void listExams_filterByCourse_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String courseId = createCourse(admin.token());
    createExam(admin.token(), courseId);

    ResponseEntity<String> res =
        get("/exams?courseId=" + courseId + "&page=0&size=20", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode content = objectMapper.readTree(res.getBody()).get("content");
    for (JsonNode node : content) {
      assertEquals(courseId, node.get("courseId").asText());
    }
  }

  @Test
  @SneakyThrows
  void getExam_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String courseId = createCourse(admin.token());
    String id = createExam(admin.token(), courseId);

    ResponseEntity<String> res = get("/exams/" + id, admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(id, objectMapper.readTree(res.getBody()).get("id").asText());
  }

  @Test
  @SneakyThrows
  void getExam_unknownId_notFound() {
    var admin = registerAndLogin(Role.ADMIN);

    ResponseEntity<String> res = get("/exams/" + UUID.randomUUID(), admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void updateExam_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String courseId = createCourse(admin.token());
    String id = createExam(admin.token(), courseId);

    ResponseEntity<String> res =
        put(
            "/exams/" + id,
            json("{\"courseId\":\"" + courseId + "\",\"title\":\"Retake\",\"coefficient\":3.0}"),
            admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertEquals("Retake", body.get("title").asText());
    assertEquals("3.0", body.get("coefficient").asText());
  }

  @Test
  @SneakyThrows
  void updateExam_asTeacherNotOwning_forbidden() {
    var admin = registerAndLogin(Role.ADMIN);
    String courseId = createCourse(admin.token());
    String id = createExam(admin.token(), courseId);
    var teacher = registerAndLogin(Role.TEACHER);

    ResponseEntity<String> res =
        put(
            "/exams/" + id,
            json("{\"courseId\":\"" + courseId + "\",\"coefficient\":1.0}"),
            teacher.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void deleteExam_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String courseId = createCourse(admin.token());
    String id = createExam(admin.token(), courseId);

    ResponseEntity<String> res = delete("/exams/" + id, admin.token());

    assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void deleteExam_asTeacherNotOwning_forbidden() {
    var admin = registerAndLogin(Role.ADMIN);
    String courseId = createCourse(admin.token());
    String id = createExam(admin.token(), courseId);
    var teacher = registerAndLogin(Role.TEACHER);

    ResponseEntity<String> res = delete("/exams/" + id, teacher.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }
}
