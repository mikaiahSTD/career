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

class DegreeIT extends AbstractControllerIT {

  @Test
  @SneakyThrows
  void getDegree_allGradesAbove10_passed() {
    var admin = registerAndLogin(Role.ADMIN);
    var student = registerAndLogin(Role.STUDENT);
    String careerId = createCareer(admin.token());
    String courseId1 = createCourseWithCredits(admin.token(), 10);
    String courseId2 = createCourseWithCredits(admin.token(), 20);
    createCareerCourse(admin.token(), careerId, courseId1, 1);
    createCareerCourse(admin.token(), careerId, courseId2, 1);
    String exam1 = createExam(admin.token(), courseId1);
    String exam2 = createExam(admin.token(), courseId2);
    createGradeWithValue(admin.token(), exam1, student.id().toString(), 15.0);
    createGradeWithValue(admin.token(), exam2, student.id().toString(), 12.0);

    ResponseEntity<String> res = get("/students/" + student.id() + "/degrees", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.get("passed").asBoolean());
    assertEquals(30, body.get("totalCredits").asInt());
  }

  @Test
  @SneakyThrows
  void getDegree_oneGradeBelow10_notPassed() {
    var admin = registerAndLogin(Role.ADMIN);
    var student = registerAndLogin(Role.STUDENT);
    String careerId = createCareer(admin.token());
    String courseId1 = createCourseWithCredits(admin.token(), 10);
    String courseId2 = createCourseWithCredits(admin.token(), 20);
    createCareerCourse(admin.token(), careerId, courseId1, 1);
    createCareerCourse(admin.token(), careerId, courseId2, 1);
    String exam1 = createExam(admin.token(), courseId1);
    String exam2 = createExam(admin.token(), courseId2);
    createGradeWithValue(admin.token(), exam1, student.id().toString(), 15.0);
    createGradeWithValue(admin.token(), exam2, student.id().toString(), 8.0);

    ResponseEntity<String> res = get("/students/" + student.id() + "/degrees", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertFalse(body.get("passed").asBoolean());
    assertEquals(10, body.get("totalCredits").asInt());
  }

  @Test
  @SneakyThrows
  void getDegree_studentCanViewOwn_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var student = registerAndLogin(Role.STUDENT);
    String careerId = createCareer(admin.token());
    String courseId = createCourseWithCredits(admin.token(), 10);
    createCareerCourse(admin.token(), careerId, courseId, 1);
    String exam = createExam(admin.token(), courseId);
    createGradeWithValue(admin.token(), exam, student.id().toString(), 12.0);

    ResponseEntity<String> res = get("/students/" + student.id() + "/degrees", student.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void getDegree_studentCannotViewOther_forbidden() {
    var admin = registerAndLogin(Role.ADMIN);
    var student1 = registerAndLogin(Role.STUDENT);
    var student2 = registerAndLogin(Role.STUDENT);

    ResponseEntity<String> res = get("/students/" + student1.id() + "/degrees", student2.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void getDegree_unknownStudent_notFound() {
    var admin = registerAndLogin(Role.ADMIN);

    ResponseEntity<String> res = get("/students/" + UUID.randomUUID() + "/degrees", admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void getDegree_noGrades_emptyCourseGrades() {
    var admin = registerAndLogin(Role.ADMIN);
    var student = registerAndLogin(Role.STUDENT);
    String careerId = createCareer(admin.token());
    String courseId = createCourseWithCredits(admin.token(), 10);
    createCareerCourse(admin.token(), careerId, courseId, 1);

    ResponseEntity<String> res = get("/students/" + student.id() + "/degrees", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertFalse(body.get("passed").asBoolean());
    assertEquals(0, body.get("totalCredits").asInt());
  }

  private String createCourseWithCredits(String adminToken, int credits) throws Exception {
    return idOf(
        post(
            "/courses",
            json(
                "{\"ref\":\"CRS-"
                    + uuid()
                    + "\",\"title\":\"Course\",\"credits\":"
                    + credits
                    + "}"),
            adminToken));
  }

  private String createGradeWithValue(
      String adminToken, String examId, String studentId, double value) throws Exception {
    return idOf(
        post(
            "/grades",
            json(
                "{\"examId\":\""
                    + examId
                    + "\",\"studentId\":\""
                    + studentId
                    + "\",\"value\":"
                    + value
                    + "}"),
            adminToken));
  }
}
