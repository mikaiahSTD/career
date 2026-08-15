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

class CourseTeacherIT extends AbstractControllerIT {

  @Test
  @SneakyThrows
  void createCourseTeacher_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String courseId = createCourse(admin.token());
    var teacher = registerAndLogin(Role.TEACHER);

    ResponseEntity<String> res =
        post(
            "/course-teachers",
            json("{\"courseId\":\"" + courseId + "\",\"teacherId\":\"" + teacher.id() + "\"}"),
            admin.token());

    assertEquals(HttpStatus.CREATED, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.has("id"));
    assertEquals(courseId, body.get("courseId").asText());
    assertEquals(teacher.id().toString(), body.get("teacherId").asText());
  }

  @Test
  @SneakyThrows
  void createCourseTeacher_unknownCourse_notFound() {
    var admin = registerAndLogin(Role.ADMIN);
    var teacher = registerAndLogin(Role.TEACHER);

    ResponseEntity<String> res =
        post(
            "/course-teachers",
            json(
                "{\"courseId\":\""
                    + UUID.randomUUID()
                    + "\",\"teacherId\":\""
                    + teacher.id()
                    + "\"}"),
            admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createCourseTeacher_unknownTeacher_notFound() {
    var admin = registerAndLogin(Role.ADMIN);
    String courseId = createCourse(admin.token());

    ResponseEntity<String> res =
        post(
            "/course-teachers",
            json("{\"courseId\":\"" + courseId + "\",\"teacherId\":\"" + UUID.randomUUID() + "\"}"),
            admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createCourseTeacher_nonTeacher_badRequest() {
    var admin = registerAndLogin(Role.ADMIN);
    String courseId = createCourse(admin.token());
    var student = registerAndLogin(Role.STUDENT);

    ResponseEntity<String> res =
        post(
            "/course-teachers",
            json("{\"courseId\":\"" + courseId + "\",\"teacherId\":\"" + student.id() + "\"}"),
            admin.token());

    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createCourseTeacher_duplicate_conflict() {
    var admin = registerAndLogin(Role.ADMIN);
    String courseId = createCourse(admin.token());
    var teacher = registerAndLogin(Role.TEACHER);
    post(
        "/course-teachers",
        json("{\"courseId\":\"" + courseId + "\",\"teacherId\":\"" + teacher.id() + "\"}"),
        admin.token());

    ResponseEntity<String> res =
        post(
            "/course-teachers",
            json("{\"courseId\":\"" + courseId + "\",\"teacherId\":\"" + teacher.id() + "\"}"),
            admin.token());

    assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createCourseTeacher_asStudent_forbidden() {
    var student = registerAndLogin(Role.STUDENT);
    var admin = registerAndLogin(Role.ADMIN);
    String courseId = createCourse(admin.token());
    var teacher = registerAndLogin(Role.TEACHER);

    ResponseEntity<String> res =
        post(
            "/course-teachers",
            json("{\"courseId\":\"" + courseId + "\",\"teacherId\":\"" + teacher.id() + "\"}"),
            student.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void listCourseTeachers_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String courseId = createCourse(admin.token());
    var teacher = registerAndLogin(Role.TEACHER);
    createCourseTeacher(admin.token(), courseId, teacher.id().toString());

    ResponseEntity<String> res = get("/course-teachers?page=0&size=20", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.has("content"));
    assertTrue(body.get("content").isArray());
  }

  @Test
  @SneakyThrows
  void listCourseTeachers_filterByTeacher_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String courseId = createCourse(admin.token());
    var teacher = registerAndLogin(Role.TEACHER);
    createCourseTeacher(admin.token(), courseId, teacher.id().toString());

    ResponseEntity<String> res =
        get("/course-teachers?teacherId=" + teacher.id() + "&page=0&size=20", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode content = objectMapper.readTree(res.getBody()).get("content");
    for (JsonNode node : content) {
      assertEquals(teacher.id().toString(), node.get("teacherId").asText());
    }
  }

  @Test
  @SneakyThrows
  void getCourseTeacher_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String courseId = createCourse(admin.token());
    var teacher = registerAndLogin(Role.TEACHER);
    String id = createCourseTeacher(admin.token(), courseId, teacher.id().toString());

    ResponseEntity<String> res = get("/course-teachers/" + id, admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(id, objectMapper.readTree(res.getBody()).get("id").asText());
  }

  @Test
  @SneakyThrows
  void getCourseTeacher_unknownId_notFound() {
    var admin = registerAndLogin(Role.ADMIN);

    ResponseEntity<String> res = get("/course-teachers/" + UUID.randomUUID(), admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void updateCourseTeacher_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String courseId = createCourse(admin.token());
    var teacher = registerAndLogin(Role.TEACHER);
    String id = createCourseTeacher(admin.token(), courseId, teacher.id().toString());
    var newTeacher = registerAndLogin(Role.TEACHER);

    ResponseEntity<String> res =
        put(
            "/course-teachers/" + id,
            json("{\"courseId\":\"" + courseId + "\",\"teacherId\":\"" + newTeacher.id() + "\"}"),
            admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(
        newTeacher.id().toString(), objectMapper.readTree(res.getBody()).get("teacherId").asText());
  }

  @Test
  @SneakyThrows
  void deleteCourseTeacher_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String courseId = createCourse(admin.token());
    var teacher = registerAndLogin(Role.TEACHER);
    String id = createCourseTeacher(admin.token(), courseId, teacher.id().toString());

    ResponseEntity<String> res = delete("/course-teachers/" + id, admin.token());

    assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
  }
}
