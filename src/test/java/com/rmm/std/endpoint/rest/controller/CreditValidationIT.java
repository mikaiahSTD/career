package com.rmm.std.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.rmm.std.constant.Role;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class CreditValidationIT extends AbstractControllerIT {

  @Test
  @SneakyThrows
  void createCareerCourse_withinLimits_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String careerId = createCareer(admin.token());
    String courseId1 = createCourseWithCredits(admin.token(), 10);
    String courseId2 = createCourseWithCredits(admin.token(), 20);

    ResponseEntity<String> res1 = createCareerCourseResponse(admin.token(), careerId, courseId1, 1);
    ResponseEntity<String> res2 = createCareerCourseResponse(admin.token(), careerId, courseId2, 1);

    assertEquals(HttpStatus.CREATED, res1.getStatusCode());
    assertEquals(HttpStatus.CREATED, res2.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createCareerCourse_exceedsSemesterLimit_badRequest() {
    var admin = registerAndLogin(Role.ADMIN);
    String careerId = createCareer(admin.token());
    String courseId1 = createCourseWithCredits(admin.token(), 20);
    String courseId2 = createCourseWithCredits(admin.token(), 15);

    createCareerCourseResponse(admin.token(), careerId, courseId1, 1);
    ResponseEntity<String> res = createCareerCourseResponse(admin.token(), careerId, courseId2, 1);

    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createCareerCourse_exceedsYearLimit_badRequest() {
    var admin = registerAndLogin(Role.ADMIN);
    String careerId = createCareer(admin.token());
    String courseId1 = createCourseWithCredits(admin.token(), 30);
    String courseId2 = createCourseWithCredits(admin.token(), 31);

    createCareerCourseResponse(admin.token(), careerId, courseId1, 1);
    ResponseEntity<String> res = createCareerCourseResponse(admin.token(), careerId, courseId2, 2);

    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createCareerCourse_exactlySemesterLimit_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String careerId = createCareer(admin.token());
    String courseId1 = createCourseWithCredits(admin.token(), 15);
    String courseId2 = createCourseWithCredits(admin.token(), 15);

    ResponseEntity<String> res1 = createCareerCourseResponse(admin.token(), careerId, courseId1, 1);
    ResponseEntity<String> res2 = createCareerCourseResponse(admin.token(), careerId, courseId2, 1);

    assertEquals(HttpStatus.CREATED, res1.getStatusCode());
    assertEquals(HttpStatus.CREATED, res2.getStatusCode());
  }

  @Test
  @SneakyThrows
  void createCareerCourse_exactlyYearLimit_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String careerId = createCareer(admin.token());
    String courseId1 = createCourseWithCredits(admin.token(), 30);
    String courseId2 = createCourseWithCredits(admin.token(), 30);

    ResponseEntity<String> res1 = createCareerCourseResponse(admin.token(), careerId, courseId1, 1);
    ResponseEntity<String> res2 = createCareerCourseResponse(admin.token(), careerId, courseId2, 2);

    assertEquals(HttpStatus.CREATED, res1.getStatusCode());
    assertEquals(HttpStatus.CREATED, res2.getStatusCode());
  }

  @Test
  @SneakyThrows
  void updateCareerCourse_withinLimits_ok() {
    var admin = registerAndLogin(Role.ADMIN);
    String careerId = createCareer(admin.token());
    String courseId1 = createCourseWithCredits(admin.token(), 10);
    String courseId2 = createCourseWithCredits(admin.token(), 5);
    String id = createCareerCourse(admin.token(), careerId, courseId1, 1);

    ResponseEntity<String> res =
        put(
            "/career-courses/" + id,
            json(
                "{\"careerId\":\""
                    + careerId
                    + "\",\"courseId\":\""
                    + courseId2
                    + "\",\"semesterNumber\":1}"),
            admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void updateCareerCourse_exceedsSemesterLimit_badRequest() {
    var admin = registerAndLogin(Role.ADMIN);
    String careerId = createCareer(admin.token());
    String courseId1 = createCourseWithCredits(admin.token(), 20);
    String courseId2 = createCourseWithCredits(admin.token(), 15);
    String id = createCareerCourse(admin.token(), careerId, courseId1, 1);

    ResponseEntity<String> res =
        put(
            "/career-courses/" + id,
            json(
                "{\"careerId\":\""
                    + careerId
                    + "\",\"courseId\":\""
                    + courseId2
                    + "\",\"semesterNumber\":1}"),
            admin.token());

    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
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

  private ResponseEntity<String> createCareerCourseResponse(
      String adminToken, String careerId, String courseId, int semesterNumber) throws Exception {
    return post(
        "/career-courses",
        json(
            "{\"careerId\":\""
                + careerId
                + "\",\"courseId\":\""
                + courseId
                + "\",\"semesterNumber\":"
                + semesterNumber
                + "}"),
        adminToken);
  }
}
