package com.rmm.std.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmm.std.conf.FacadeIT;
import com.rmm.std.constant.Role;
import com.rmm.std.dto.LoginRequest;
import com.rmm.std.dto.UserRequest;
import com.rmm.std.endpoint.event.EventProducer;
import com.rmm.std.endpoint.event.model.SendTranscriptEmailRequested;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

class TranscriptIT extends FacadeIT {

  @LocalServerPort private int port;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private EventProducer<SendTranscriptEmailRequested> eventProducer;

  private final RestTemplate restTemplate = new RestTemplate();

  @Test
  @SneakyThrows
  void sendTranscript_asOwnStudent_returns202AndProducesEvent() {
    var req = randomUser();
    var registration = registerAndLogin(req);
    var studentId = registration.id();

    ResponseEntity<String> res =
        post(
            "/students/" + studentId + "/transcript/send",
            "{\"semesterId\":null}",
            registration.token());

    assertEquals(HttpStatus.ACCEPTED, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertNotNull(body.get("message"));

    verify(eventProducer).accept(any());
  }

  @Test
  @SneakyThrows
  void sendTranscript_otherStudent_forbidden() {
    var own = randomUser();
    var ownRegistration = registerAndLogin(own);
    var other = randomUser();
    var otherRegistration = registerAndLogin(other);

    ResponseEntity<String> res =
        post(
            "/students/" + otherRegistration.id() + "/transcript/send",
            "{\"semesterId\":null}",
            ownRegistration.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void sendTranscript_unknownStudent_notFound() {
    var req = randomUser();
    var registration = registerAndLogin(req);

    ResponseEntity<String> res =
        post(
            "/students/" + UUID.randomUUID() + "/transcript/send",
            "{\"semesterId\":null}",
            registration.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  private Registration registerAndLogin(UserRequest req) throws Exception {
    ResponseEntity<String> registerRes = post("/auth/register", json(req), null);
    assertEquals(HttpStatus.OK, registerRes.getStatusCode());
    UUID id = UUID.fromString(objectMapper.readTree(registerRes.getBody()).get("id").asText());
    ResponseEntity<String> loginRes =
        post("/auth/login", json(new LoginRequest(req.getEmail(), req.getPassword())), null);
    assertEquals(HttpStatus.OK, loginRes.getStatusCode());
    String token = objectMapper.readTree(loginRes.getBody()).get("token").asText();
    return new Registration(id, token);
  }

  private record Registration(UUID id, String token) {}

  private UserRequest randomUser() {
    String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    return UserRequest.builder()
        .ref("ref-" + suffix)
        .firstname("John")
        .lastname("Doe")
        .email("john-" + suffix + "@test.com")
        .password("P@ssw0rd-" + suffix)
        .role(Role.STUDENT)
        .build();
  }

  @SneakyThrows
  private String json(Object value) {
    return objectMapper.writeValueAsString(value);
  }

  private ResponseEntity<String> post(String path, String jsonBody, String bearerToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (bearerToken != null) {
      headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
    }
    HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
    return send(() -> restTemplate.exchange(url(path), HttpMethod.POST, entity, String.class));
  }

  private ResponseEntity<String> send(Supplier<ResponseEntity<String>> request) {
    try {
      return request.get();
    } catch (HttpClientErrorException e) {
      return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
    }
  }

  private String url(String path) {
    return "http://localhost:" + port + path;
  }
}
