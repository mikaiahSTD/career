package com.rmm.std.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmm.std.conf.FacadeIT;
import com.rmm.std.constant.Role;
import com.rmm.std.dto.LoginRequest;
import com.rmm.std.dto.UserRequest;
import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

abstract class AbstractControllerIT extends FacadeIT {

  @LocalServerPort protected int port;

  @Autowired protected ObjectMapper objectMapper;

  protected final RestTemplate restTemplate;

  {
    var httpClient =
        java.net.http.HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    var factory = new org.springframework.http.client.JdkClientHttpRequestFactory(httpClient);
    factory.setReadTimeout(Duration.ofSeconds(10));
    restTemplate = new RestTemplate(factory);
  }

  protected record Registration(UUID id, String email, String token) {}

  protected Registration registerAndLogin(Role role) throws Exception {
    UserRequest req = randomUser(role);
    ResponseEntity<String> registerRes = post("/auth/register", json(req), null);
    assertEquals(HttpStatus.OK, registerRes.getStatusCode());
    UUID id = UUID.fromString(objectMapper.readTree(registerRes.getBody()).get("id").asText());
    ResponseEntity<String> loginRes =
        post("/auth/login", json(new LoginRequest(req.getEmail(), req.getPassword())), null);
    assertEquals(HttpStatus.OK, loginRes.getStatusCode());
    String token = objectMapper.readTree(loginRes.getBody()).get("token").asText();
    return new Registration(id, req.getEmail(), token);
  }

  protected UserRequest randomUser(Role role) {
    String suffix = uuid();
    return UserRequest.builder()
        .ref("ref-" + suffix)
        .firstname("John")
        .lastname("Doe")
        .email("john-" + suffix + "@test.com")
        .password("p@ssw0rd-" + suffix)
        .role(role)
        .build();
  }

  protected String uuid() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
  }

  @SneakyThrows
  protected String json(Object value) {
    if (value instanceof String raw) {
      return raw;
    }
    return objectMapper.writeValueAsString(value);
  }

  protected ResponseEntity<String> post(String path, String jsonBody, String bearerToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (bearerToken != null) {
      headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
    }
    HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
    return send(() -> restTemplate.exchange(url(path), HttpMethod.POST, entity, String.class));
  }

  protected ResponseEntity<String> get(String path, String bearerToken) {
    HttpHeaders headers = new HttpHeaders();
    if (bearerToken != null) {
      headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
    }
    HttpEntity<Void> entity = new HttpEntity<>(headers);
    return send(() -> restTemplate.exchange(url(path), HttpMethod.GET, entity, String.class));
  }

  protected ResponseEntity<String> put(String path, String jsonBody, String bearerToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (bearerToken != null) {
      headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
    }
    HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
    return send(() -> restTemplate.exchange(url(path), HttpMethod.PUT, entity, String.class));
  }

  protected ResponseEntity<String> patch(String path, String jsonBody, String bearerToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (bearerToken != null) {
      headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
    }
    HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
    return send(() -> restTemplate.exchange(url(path), HttpMethod.PATCH, entity, String.class));
  }

  protected ResponseEntity<String> delete(String path, String bearerToken) {
    HttpHeaders headers = new HttpHeaders();
    if (bearerToken != null) {
      headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
    }
    HttpEntity<Void> entity = new HttpEntity<>(headers);
    return send(() -> restTemplate.exchange(url(path), HttpMethod.DELETE, entity, String.class));
  }

  protected ResponseEntity<String> send(Supplier<ResponseEntity<String>> request) {
    try {
      return request.get();
    } catch (HttpClientErrorException e) {
      return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
    } catch (HttpServerErrorException e) {
      return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
    }
  }

  protected String url(String path) {
    return "http://localhost:" + port + path;
  }

  @SneakyThrows
  protected String idOf(ResponseEntity<String> res) {
    return objectMapper.readTree(res.getBody()).get("id").asText();
  }

  protected String createCareer(String adminToken) throws Exception {
    return idOf(
        post(
            "/careers",
            json("{\"title\":\"Informatique\",\"specialization\":\"EL\"}"),
            adminToken));
  }

  protected String createPromotion(String adminToken) throws Exception {
    return idOf(
        post(
            "/promotions", json("{\"label\":\"P" + uuid() + "\",\"startYear\":2024}"), adminToken));
  }

  protected String createCourse(String adminToken) throws Exception {
    return idOf(
        post(
            "/courses",
            json("{\"ref\":\"CRS-" + uuid() + "\",\"title\":\"Algo\",\"credits\":5}"),
            adminToken));
  }

  protected String createSemester(String adminToken, String promotionId) throws Exception {
    return idOf(
        post(
            "/semesters",
            json("{\"promotionId\":\"" + promotionId + "\",\"number\":1}"),
            adminToken));
  }

  protected String createGroup(String adminToken, String promotionId, String careerId)
      throws Exception {
    return idOf(
        post(
            "/groups",
            json(
                "{\"ref\":\"G-"
                    + uuid()
                    + "\",\"promotionId\":\""
                    + promotionId
                    + "\",\"careerId\":\""
                    + careerId
                    + "\"}"),
            adminToken));
  }

  protected String createCourseTeacher(String adminToken, String courseId, String teacherId)
      throws Exception {
    return idOf(
        post(
            "/course-teachers",
            json("{\"courseId\":\"" + courseId + "\",\"teacherId\":\"" + teacherId + "\"}"),
            adminToken));
  }

  protected String createCourseGroup(
      String adminToken, String courseId, String teacherId, String groupId) throws Exception {
    return idOf(
        post(
            "/course-groups",
            json(
                "{\"courseId\":\""
                    + courseId
                    + "\",\"teacherId\":\""
                    + teacherId
                    + "\",\"groupId\":\""
                    + groupId
                    + "\"}"),
            adminToken));
  }

  protected String createExam(String adminToken, String courseId) throws Exception {
    String promotionId = createPromotion(adminToken);
    String semesterId = createSemester(adminToken, promotionId);
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

  protected String createUserGroup(String adminToken, String userId, String groupId)
      throws Exception {
    return idOf(
        post(
            "/user-groups",
            json("{\"userId\":\"" + userId + "\",\"groupId\":\"" + groupId + "\"}"),
            adminToken));
  }

  protected String createUserPromotion(String adminToken, String userId, String promotionId)
      throws Exception {
    return idOf(
        post(
            "/user-promotions",
            json("{\"userId\":\"" + userId + "\",\"promotionId\":\"" + promotionId + "\"}"),
            adminToken));
  }

  protected String createGrade(String adminToken, String examId, String studentId)
      throws Exception {
    return idOf(
        post(
            "/grades",
            json(
                "{\"examId\":\""
                    + examId
                    + "\",\"studentId\":\""
                    + studentId
                    + "\",\"value\":12.5}"),
            adminToken));
  }

  protected String createCareerCourse(
      String adminToken, String careerId, String courseId, int semesterNumber) throws Exception {
    return idOf(
        post(
            "/career-courses",
            json(
                "{\"careerId\":\""
                    + careerId
                    + "\",\"courseId\":\""
                    + courseId
                    + "\",\"semesterNumber\":"
                    + semesterNumber
                    + "}"),
            adminToken));
  }
}
