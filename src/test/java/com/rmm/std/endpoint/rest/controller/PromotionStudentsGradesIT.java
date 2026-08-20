package com.rmm.std.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.rmm.std.constant.Role;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class PromotionStudentsGradesIT extends AbstractControllerIT {

  @Test
  @SneakyThrows
  void getStudentsGrades_withGrades_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var student = registerAndLogin(Role.STUDENT);
    String promotionId = createPromotion(admin.token());
    String semesterId = createSemester(admin.token(), promotionId);
    String careerId = createCareer(admin.token());
    String courseId = createCourse(admin.token());
    createCareerCourse(admin.token(), careerId, courseId, 1);
    String groupId = createGroup(admin.token(), promotionId, careerId);
    createUserGroup(admin.token(), student.id().toString(), groupId);
    createUserPromotion(admin.token(), student.id().toString(), promotionId);
    String examId = createExamWithSemester(admin.token(), courseId, semesterId);
    createGradeWithValue(admin.token(), examId, student.id().toString(), 15.0);

    ResponseEntity<String> res =
        get("/promotions/" + promotionId + "/students-grades", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertEquals(promotionId, body.get("promotionId").asText());
    assertNotNull(body.get("promotionLabel"));
    assertTrue(body.get("students").isArray());
    assertTrue(body.get("students").size() > 0);

    JsonNode studentNode = body.get("students").get(0);
    assertEquals(student.id().toString(), studentNode.get("studentId").asText());
    assertTrue(studentNode.get("courseGrades").isArray());
    assertTrue(studentNode.get("courseGrades").size() > 0);

    JsonNode gradeNode = studentNode.get("courseGrades").get(0);
    assertEquals(15.0, gradeNode.get("grade").asDouble(), 0.01);
    assertNotNull(gradeNode.get("courseTitle"));
    assertNotNull(gradeNode.get("examTitle"));
  }

  @Test
  @SneakyThrows
  void getStudentsGrades_noGrades_emptyStudents() {
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());

    ResponseEntity<String> res =
        get("/promotions/" + promotionId + "/students-grades", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertEquals(promotionId, body.get("promotionId").asText());
    assertTrue(body.get("students").isArray());
    assertEquals(0, body.get("students").size());
  }

  @Test
  @SneakyThrows
  void getStudentsGrades_enrolledStudentWithoutGrades_stillShown() {
    var admin = registerAndLogin(Role.ADMIN);
    var student = registerAndLogin(Role.STUDENT);
    String promotionId = createPromotion(admin.token());
    createUserPromotion(admin.token(), student.id().toString(), promotionId);

    ResponseEntity<String> res =
        get("/promotions/" + promotionId + "/students-grades", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertEquals(1, body.get("students").size());
    JsonNode studentNode = body.get("students").get(0);
    assertEquals(student.id().toString(), studentNode.get("studentId").asText());
    assertTrue(studentNode.get("courseGrades").isArray());
    assertEquals(0, studentNode.get("courseGrades").size());
  }

  @Test
  @SneakyThrows
  void getStudentsGrades_unknownPromotion_notFound() {
    var admin = registerAndLogin(Role.ADMIN);

    ResponseEntity<String> res =
        get("/promotions/" + java.util.UUID.randomUUID() + "/students-grades", admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void getStudentsGrades_multipleStudents_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var student1 = registerAndLogin(Role.STUDENT);
    var student2 = registerAndLogin(Role.STUDENT);
    String promotionId = createPromotion(admin.token());
    String semesterId = createSemester(admin.token(), promotionId);
    String careerId = createCareer(admin.token());
    String courseId = createCourse(admin.token());
    createCareerCourse(admin.token(), careerId, courseId, 1);
    String groupId = createGroup(admin.token(), promotionId, careerId);
    createUserGroup(admin.token(), student1.id().toString(), groupId);
    createUserGroup(admin.token(), student2.id().toString(), groupId);
    createUserPromotion(admin.token(), student1.id().toString(), promotionId);
    createUserPromotion(admin.token(), student2.id().toString(), promotionId);
    String examId = createExamWithSemester(admin.token(), courseId, semesterId);
    createGradeWithValue(admin.token(), examId, student1.id().toString(), 15.0);
    createGradeWithValue(admin.token(), examId, student2.id().toString(), 12.0);

    ResponseEntity<String> res =
        get("/promotions/" + promotionId + "/students-grades", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertEquals(2, body.get("students").size());
  }

  @Test
  @SneakyThrows
  void getStudentsGrades_asStudent_forbidden() {
    var admin = registerAndLogin(Role.ADMIN);
    var student = registerAndLogin(Role.STUDENT);
    String promotionId = createPromotion(admin.token());

    ResponseEntity<String> res =
        get("/promotions/" + promotionId + "/students-grades", student.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void getStudentsGrades_asTeacher_onlyOwnCourses() {
    var admin = registerAndLogin(Role.ADMIN);
    var teacher = registerAndLogin(Role.TEACHER);
    var student = registerAndLogin(Role.STUDENT);
    String promotionId = createPromotion(admin.token());
    String semesterId = createSemester(admin.token(), promotionId);
    String careerId = createCareer(admin.token());

    String taughtCourseId = createCourseWithRef(admin.token(), "CRS-TAUGHT");
    String otherCourseId = createCourseWithRef(admin.token(), "CRS-OTHER");
    createCareerCourse(admin.token(), careerId, taughtCourseId, 1);
    createCareerCourse(admin.token(), careerId, otherCourseId, 1);
    createCourseTeacher(admin.token(), taughtCourseId, teacher.id().toString());

    String groupId = createGroup(admin.token(), promotionId, careerId);
    createUserGroup(admin.token(), student.id().toString(), groupId);
    createUserPromotion(admin.token(), student.id().toString(), promotionId);

    String taughtExam = createExamWithSemester(admin.token(), taughtCourseId, semesterId);
    String otherExam = createExamWithSemester(admin.token(), otherCourseId, semesterId);
    createGradeWithValue(admin.token(), taughtExam, student.id().toString(), 15.0);
    createGradeWithValue(admin.token(), otherExam, student.id().toString(), 12.0);

    ResponseEntity<String> res =
        get("/promotions/" + promotionId + "/students-grades", teacher.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertEquals(1, body.get("students").size());
    JsonNode studentNode = body.get("students").get(0);
    assertEquals(student.id().toString(), studentNode.get("studentId").asText());

    JsonNode courseGrades = studentNode.get("courseGrades");
    assertEquals(1, courseGrades.size());
    assertEquals("Course CRS-TAUGHT", courseGrades.get(0).get("courseTitle").asText());
    assertEquals(15.0, courseGrades.get(0).get("grade").asDouble(), 0.01);
  }

  @Test
  @SneakyThrows
  void getStudentsGrades_multipleCourses_sorted() {
    var admin = registerAndLogin(Role.ADMIN);
    var student = registerAndLogin(Role.STUDENT);
    String promotionId = createPromotion(admin.token());
    String semesterId = createSemester(admin.token(), promotionId);
    String careerId = createCareer(admin.token());
    String courseId1 = createCourseWithRef(admin.token(), "CRS-Z");
    String courseId2 = createCourseWithRef(admin.token(), "CRS-A");
    createCareerCourse(admin.token(), careerId, courseId1, 1);
    createCareerCourse(admin.token(), careerId, courseId2, 1);
    String groupId = createGroup(admin.token(), promotionId, careerId);
    createUserGroup(admin.token(), student.id().toString(), groupId);
    createUserPromotion(admin.token(), student.id().toString(), promotionId);
    String exam1 = createExamWithSemester(admin.token(), courseId1, semesterId);
    String exam2 = createExamWithSemester(admin.token(), courseId2, semesterId);
    createGradeWithValue(admin.token(), exam1, student.id().toString(), 15.0);
    createGradeWithValue(admin.token(), exam2, student.id().toString(), 12.0);

    ResponseEntity<String> res =
        get("/promotions/" + promotionId + "/students-grades", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    JsonNode courseGrades = body.get("students").get(0).get("courseGrades");
    assertEquals(2, courseGrades.size());
    String first = courseGrades.get(0).get("courseTitle").asText();
    String second = courseGrades.get(1).get("courseTitle").asText();
    assertTrue(first.compareTo(second) <= 0);
  }

  private String createExamWithSemester(String adminToken, String courseId, String semesterId)
      throws Exception {
    return idOf(
        post(
            "/exams",
            json(
                "{\"courseId\":\""
                    + courseId
                    + "\",\"semesterId\":\""
                    + semesterId
                    + "\",\"title\":\"Midterm\",\"coefficient\":2.0}"),
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

  private String createCourseWithRef(String adminToken, String ref) throws Exception {
    return idOf(
        post(
            "/courses",
            json("{\"ref\":\"" + ref + "\",\"title\":\"Course " + ref + "\",\"credits\":10}"),
            adminToken));
  }
}
