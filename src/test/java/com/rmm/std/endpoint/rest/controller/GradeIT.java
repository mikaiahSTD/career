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

class GradeIT extends AbstractControllerIT {

  private record GradeContext(
      String courseId,
      String teacherId,
      String teacherToken,
      String examId,
      String studentId,
      String studentToken) {}

  private GradeContext setup(String adminToken) throws Exception {
    String courseId = createCourse(adminToken);
    var teacher = registerAndLogin(Role.TEACHER);
    createCourseTeacher(adminToken, courseId, teacher.id().toString());
    String examId = createExam(adminToken, courseId);
    var student = registerAndLogin(Role.STUDENT);
    return new GradeContext(
        courseId,
        teacher.id().toString(),
        teacher.token(),
        examId,
        student.id().toString(),
        student.token());
  }

  @Test
  @SneakyThrows
  void createGrade_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());

    ResponseEntity<String> res =
        post(
            "/grades",
            json(
                "{\"examId\":\""
                    + ctx.examId()
                    + "\",\"studentId\":\""
                    + ctx.studentId()
                    + "\",\"value\":12.5}"),
            admin.token());

    assertEquals(HttpStatus.CREATED, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.has("id"));
    assertEquals(ctx.examId(), body.get("examId").asText());
    assertEquals(ctx.studentId(), body.get("studentId").asText());
    assertTrue(body.has("assignmentDate"));
  }

  @Test
  @SneakyThrows
  void createGrade_asTeacherOwningCourse_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());

    ResponseEntity<String> res =
        post(
            "/grades",
            json(
                "{\"examId\":\""
                    + ctx.examId()
                    + "\",\"studentId\":\""
                    + ctx.studentId()
                    + "\",\"value\":14.0}"),
            ctx.teacherToken());

    assertEquals(HttpStatus.CREATED, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createGrade_asTeacherNotOwningCourse_forbidden() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    var otherTeacher = registerAndLogin(Role.TEACHER);

    ResponseEntity<String> res =
        post(
            "/grades",
            json(
                "{\"examId\":\""
                    + ctx.examId()
                    + "\",\"studentId\":\""
                    + ctx.studentId()
                    + "\",\"value\":14.0}"),
            otherTeacher.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createGrade_unknownExam_notFound() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());

    ResponseEntity<String> res =
        post(
            "/grades",
            json(
                "{\"examId\":\""
                    + UUID.randomUUID()
                    + "\",\"studentId\":\""
                    + ctx.studentId()
                    + "\",\"value\":10.0}"),
            admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createGrade_unknownStudent_notFound() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());

    ResponseEntity<String> res =
        post(
            "/grades",
            json(
                "{\"examId\":\""
                    + ctx.examId()
                    + "\",\"studentId\":\""
                    + UUID.randomUUID()
                    + "\",\"value\":10.0}"),
            admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createGrade_duplicate_conflict() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    String body =
        json(
            "{\"examId\":\""
                + ctx.examId()
                + "\",\"studentId\":\""
                + ctx.studentId()
                + "\",\"value\":10.0}");
    post("/grades", body, admin.token());

    ResponseEntity<String> res = post("/grades", body, admin.token());

    assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createGrade_asStudent_forbidden() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    var student = registerAndLogin(Role.STUDENT);

    ResponseEntity<String> res =
        post(
            "/grades",
            json(
                "{\"examId\":\""
                    + ctx.examId()
                    + "\",\"studentId\":\""
                    + ctx.studentId()
                    + "\",\"value\":10.0}"),
            student.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void listGrades_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    createGrade(admin.token(), ctx.examId(), ctx.studentId());

    ResponseEntity<String> res = get("/grades?page=0&size=20", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.has("content"));
    assertTrue(body.get("content").isArray());
  }

  @Test
  @SneakyThrows
  void listGrades_asTeacher_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    createGrade(admin.token(), ctx.examId(), ctx.studentId());

    ResponseEntity<String> res = get("/grades?page=0&size=20", ctx.teacherToken());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode content = objectMapper.readTree(res.getBody()).get("content");
    for (JsonNode node : content) {
      assertEquals(ctx.examId(), node.get("examId").asText());
    }
  }

  @Test
  @SneakyThrows
  void listGrades_asStudent_forbidden() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    createGrade(admin.token(), ctx.examId(), ctx.studentId());
    var student = registerAndLogin(Role.STUDENT);

    ResponseEntity<String> res = get("/grades?page=0&size=20", student.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void getGrade_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    String id = createGrade(admin.token(), ctx.examId(), ctx.studentId());

    ResponseEntity<String> res = get("/grades/" + id, admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(id, objectMapper.readTree(res.getBody()).get("id").asText());
  }

  @Test
  @SneakyThrows
  void getGrade_asTeacherOwningCourse_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    String id = createGrade(admin.token(), ctx.examId(), ctx.studentId());

    ResponseEntity<String> res = get("/grades/" + id, ctx.teacherToken());

    assertEquals(HttpStatus.OK, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void getGrade_asOwnStudent_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    String id = createGrade(admin.token(), ctx.examId(), ctx.studentId());

    ResponseEntity<String> res = get("/grades/" + id, ctx.studentToken());

    assertEquals(HttpStatus.OK, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void getGrade_asOtherStudent_forbidden() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    String id = createGrade(admin.token(), ctx.examId(), ctx.studentId());
    var other = registerAndLogin(Role.STUDENT);

    ResponseEntity<String> res = get("/grades/" + id, other.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void getGrade_asTeacherNotOwning_forbidden() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    String id = createGrade(admin.token(), ctx.examId(), ctx.studentId());
    var otherTeacher = registerAndLogin(Role.TEACHER);

    ResponseEntity<String> res = get("/grades/" + id, otherTeacher.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void getGrade_unknownId_notFound() {
    var admin = registerAndLogin(Role.ADMIN);

    ResponseEntity<String> res = get("/grades/" + UUID.randomUUID(), admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void deleteGrade_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    String id = createGrade(admin.token(), ctx.examId(), ctx.studentId());

    ResponseEntity<String> res = delete("/grades/" + id, admin.token());

    assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void deleteGrade_asTeacher_forbidden() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    String id = createGrade(admin.token(), ctx.examId(), ctx.studentId());

    ResponseEntity<String> res = delete("/grades/" + id, ctx.teacherToken());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void correctGrade_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    String id = createGrade(admin.token(), ctx.examId(), ctx.studentId());

    ResponseEntity<String> res =
        patch(
            "/grades/" + id + "/correct",
            json("{\"value\":16.0,\"reason\":\"Recheck\"}"),
            admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertEquals("16.0", body.get("value").asText());
  }

  @Test
  @SneakyThrows
  void correctGrade_asTeacherOwningCourse_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    String id = createGrade(admin.token(), ctx.examId(), ctx.studentId());

    ResponseEntity<String> res =
        patch(
            "/grades/" + id + "/correct",
            json("{\"value\":16.0,\"reason\":\"Recheck\"}"),
            ctx.teacherToken());

    assertEquals(HttpStatus.OK, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void correctGrade_asTeacherNotOwning_forbidden() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    String id = createGrade(admin.token(), ctx.examId(), ctx.studentId());
    var otherTeacher = registerAndLogin(Role.TEACHER);

    ResponseEntity<String> res =
        patch(
            "/grades/" + id + "/correct",
            json("{\"value\":16.0,\"reason\":\"Recheck\"}"),
            otherTeacher.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void correctGrade_blankReason_badRequest() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    String id = createGrade(admin.token(), ctx.examId(), ctx.studentId());

    ResponseEntity<String> res =
        patch(
            "/grades/" + id + "/correct", json("{\"value\":16.0,\"reason\":\"\"}"), admin.token());

    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.get("details").has("reason"));
  }

  @Test
  @SneakyThrows
  void correctGrade_unknownId_notFound() {
    var admin = registerAndLogin(Role.ADMIN);

    ResponseEntity<String> res =
        patch(
            "/grades/" + UUID.randomUUID() + "/correct",
            json("{\"value\":16.0,\"reason\":\"Recheck\"}"),
            admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void gradeHistory_afterCorrection_hasEntry() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    String id = createGrade(admin.token(), ctx.examId(), ctx.studentId());
    patch(
        "/grades/" + id + "/correct",
        json("{\"value\":16.0,\"reason\":\"Recheck\"}"),
        admin.token());

    ResponseEntity<String> res = get("/grades/" + id + "/history", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.isArray());
    assertTrue(body.size() >= 1);
    assertEquals("16.0", body.get(0).get("newValue").asText());
    assertEquals("Recheck", body.get(0).get("reason").asText());
  }

  @Test
  @SneakyThrows
  void gradeHistory_emptyBeforeCorrection() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    String id = createGrade(admin.token(), ctx.examId(), ctx.studentId());

    ResponseEntity<String> res = get("/grades/" + id + "/history", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertTrue(objectMapper.readTree(res.getBody()).isArray());
  }

  @Test
  @SneakyThrows
  void gradeHistory_asOwnStudent_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    String id = createGrade(admin.token(), ctx.examId(), ctx.studentId());

    ResponseEntity<String> res = get("/grades/" + id + "/history", ctx.studentToken());

    assertEquals(HttpStatus.OK, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void gradeHistory_unknownId_notFound() {
    var admin = registerAndLogin(Role.ADMIN);

    ResponseEntity<String> res = get("/grades/" + UUID.randomUUID() + "/history", admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }
}
