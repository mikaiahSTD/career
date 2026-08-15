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

class CourseGroupIT extends AbstractControllerIT {

  private record CourseGroupContext(String courseId, String teacherId, String groupId) {}

  private CourseGroupContext setup(String adminToken) throws Exception {
    String courseId = createCourse(adminToken);
    var teacher = registerAndLogin(Role.TEACHER);
    String promotionId = createPromotion(adminToken);
    String careerId = createCareer(adminToken);
    String groupId = createGroup(adminToken, promotionId, careerId);
    return new CourseGroupContext(courseId, teacher.id().toString(), groupId);
  }

  @Test
  @SneakyThrows
  void createCourseGroup_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());

    ResponseEntity<String> res =
        post(
            "/course-groups",
            json(
                "{\"courseId\":\""
                    + ctx.courseId()
                    + "\",\"teacherId\":\""
                    + ctx.teacherId()
                    + "\",\"groupId\":\""
                    + ctx.groupId()
                    + "\"}"),
            admin.token());

    assertEquals(HttpStatus.CREATED, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.has("id"));
    assertEquals(ctx.courseId(), body.get("courseId").asText());
    assertEquals(ctx.teacherId(), body.get("teacherId").asText());
    assertEquals(ctx.groupId(), body.get("groupId").asText());
  }

  @Test
  @SneakyThrows
  void createCourseGroup_unknownCourse_notFound() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());

    ResponseEntity<String> res =
        post(
            "/course-groups",
            json(
                "{\"courseId\":\""
                    + UUID.randomUUID()
                    + "\",\"teacherId\":\""
                    + ctx.teacherId()
                    + "\",\"groupId\":\""
                    + ctx.groupId()
                    + "\"}"),
            admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createCourseGroup_nonTeacher_badRequest() {
    var admin = registerAndLogin(Role.ADMIN);
    String courseId = createCourse(admin.token());
    String promotionId = createPromotion(admin.token());
    String careerId = createCareer(admin.token());
    String groupId = createGroup(admin.token(), promotionId, careerId);
    var student = registerAndLogin(Role.STUDENT);

    ResponseEntity<String> res =
        post(
            "/course-groups",
            json(
                "{\"courseId\":\""
                    + courseId
                    + "\",\"teacherId\":\""
                    + student.id()
                    + "\",\"groupId\":\""
                    + groupId
                    + "\"}"),
            admin.token());

    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createCourseGroup_unknownGroup_notFound() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());

    ResponseEntity<String> res =
        post(
            "/course-groups",
            json(
                "{\"courseId\":\""
                    + ctx.courseId()
                    + "\",\"teacherId\":\""
                    + ctx.teacherId()
                    + "\",\"groupId\":\""
                    + UUID.randomUUID()
                    + "\"}"),
            admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createCourseGroup_duplicate_conflict() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    String body =
        json(
            "{\"courseId\":\""
                + ctx.courseId()
                + "\",\"teacherId\":\""
                + ctx.teacherId()
                + "\",\"groupId\":\""
                + ctx.groupId()
                + "\"}");
    post("/course-groups", body, admin.token());

    ResponseEntity<String> res = post("/course-groups", body, admin.token());

    assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createCourseGroup_asStudent_forbidden() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    var student = registerAndLogin(Role.STUDENT);

    ResponseEntity<String> res =
        post(
            "/course-groups",
            json(
                "{\"courseId\":\""
                    + ctx.courseId()
                    + "\",\"teacherId\":\""
                    + ctx.teacherId()
                    + "\",\"groupId\":\""
                    + ctx.groupId()
                    + "\"}"),
            student.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void listCourseGroups_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    createCourseGroup(admin.token(), ctx.courseId(), ctx.teacherId(), ctx.groupId());

    ResponseEntity<String> res = get("/course-groups?page=0&size=20", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.has("content"));
    assertTrue(body.get("content").isArray());
  }

  @Test
  @SneakyThrows
  void listCourseGroups_filterByGroup_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    createCourseGroup(admin.token(), ctx.courseId(), ctx.teacherId(), ctx.groupId());

    ResponseEntity<String> res =
        get("/course-groups?groupId=" + ctx.groupId() + "&page=0&size=20", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode content = objectMapper.readTree(res.getBody()).get("content");
    for (JsonNode node : content) {
      assertEquals(ctx.groupId(), node.get("groupId").asText());
    }
  }

  @Test
  @SneakyThrows
  void getCourseGroup_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    String id = createCourseGroup(admin.token(), ctx.courseId(), ctx.teacherId(), ctx.groupId());

    ResponseEntity<String> res = get("/course-groups/" + id, admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(id, objectMapper.readTree(res.getBody()).get("id").asText());
  }

  @Test
  @SneakyThrows
  void getCourseGroup_unknownId_notFound() {
    var admin = registerAndLogin(Role.ADMIN);

    ResponseEntity<String> res = get("/course-groups/" + UUID.randomUUID(), admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void updateCourseGroup_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    String id = createCourseGroup(admin.token(), ctx.courseId(), ctx.teacherId(), ctx.groupId());
    var newTeacher = registerAndLogin(Role.TEACHER);

    ResponseEntity<String> res =
        put(
            "/course-groups/" + id,
            json(
                "{\"courseId\":\""
                    + ctx.courseId()
                    + "\",\"teacherId\":\""
                    + newTeacher.id()
                    + "\",\"groupId\":\""
                    + ctx.groupId()
                    + "\"}"),
            admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(
        newTeacher.id().toString(), objectMapper.readTree(res.getBody()).get("teacherId").asText());
  }

  @Test
  @SneakyThrows
  void deleteCourseGroup_asAdmin_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    var ctx = setup(admin.token());
    String id = createCourseGroup(admin.token(), ctx.courseId(), ctx.teacherId(), ctx.groupId());

    ResponseEntity<String> res = delete("/course-groups/" + id, admin.token());

    assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
  }
}
