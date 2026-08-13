package com.rmm.std.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmm.std.conf.FacadeIT;
import com.rmm.std.constant.Role;
import com.rmm.std.dto.LoginRequest;
import com.rmm.std.dto.UserRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.function.Supplier;
import javax.crypto.SecretKey;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

class AuthIT extends FacadeIT {

  @LocalServerPort private int port;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private Environment environment;

  private final RestTemplate restTemplate = new RestTemplate();

  @Test
  @SneakyThrows
  void register_returnsUserWithoutPassword() {
    UserRequest req = randomUser();

    ResponseEntity<String> res = post("/auth/register", json(req), null);

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertNotNull(body.get("id").asText());
    assertEquals(req.getRef(), body.get("ref").asText());
    assertEquals(req.getFirstname(), body.get("firstname").asText());
    assertEquals(req.getLastname(), body.get("lastname").asText());
    assertEquals(req.getEmail(), body.get("email").asText());
    assertEquals(req.getRole().name(), body.get("role").asText());
    assertFalse(body.has("password"));
  }

  @Test
  @SneakyThrows
  void register_duplicateEmail_conflict() {
    UserRequest req = randomUser();
    post("/auth/register", json(req), null);

    ResponseEntity<String> res = post("/auth/register", json(req), null);

    assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertEquals("CONFLICT", body.get("error").asText());
  }

  @Test
  @SneakyThrows
  void register_duplicateRef_conflict() {
    UserRequest first = randomUser();
    post("/auth/register", json(first), null);

    UserRequest second = randomUser();
    second.setRef(first.getRef());

    ResponseEntity<String> res = post("/auth/register", json(second), null);

    assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertEquals("CONFLICT", body.get("error").asText());
  }

  @Test
  @SneakyThrows
  void register_blankEmail_badRequest() {
    UserRequest req = randomUser();
    req.setEmail("");

    ResponseEntity<String> res = post("/auth/register", json(req), null);

    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.get("details").has("email"));
  }

  @Test
  @SneakyThrows
  void register_invalidEmail_badRequest() {
    UserRequest req = randomUser();
    req.setEmail("not-an-email");

    ResponseEntity<String> res = post("/auth/register", json(req), null);

    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.get("details").has("email"));
  }

  @Test
  @SneakyThrows
  void register_blankPassword_badRequest() {
    UserRequest req = randomUser();
    req.setPassword("");

    ResponseEntity<String> res = post("/auth/register", json(req), null);

    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.get("details").has("password"));
  }

  @Test
  @SneakyThrows
  void register_blankRef_badRequest() {
    UserRequest req = randomUser();
    req.setRef("");

    ResponseEntity<String> res = post("/auth/register", json(req), null);

    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.get("details").has("ref"));
  }

  @Test
  @SneakyThrows
  void register_nullRole_badRequest() {
    UserRequest req = randomUser();
    req.setRole(null);

    ResponseEntity<String> res = post("/auth/register", json(req), null);

    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.get("details").has("role"));
  }

  @Test
  @SneakyThrows
  void register_invalidRole_badRequest() {
    String body =
        "{\"ref\":\"ref-invalid-role\",\"email\":\"invalid-role@test.com\","
            + "\"password\":\"secret123\",\"role\":\"PRINCIPAL\"}";

    ResponseEntity<String> res = post("/auth/register", body, null);

    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    JsonNode resBody = objectMapper.readTree(res.getBody());
    assertTrue(resBody.get("message").asText().contains("role"));
  }

  @Test
  @SneakyThrows
  void register_malformedJson_badRequest() {
    ResponseEntity<String> res = post("/auth/register", "{ this is not valid json", null);

    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    JsonNode resBody = objectMapper.readTree(res.getBody());
    assertTrue(resBody.get("message").asText().contains("malformed"));
  }

  @Test
  @SneakyThrows
  void login_validCredentials_returnsJwt() {
    UserRequest req = randomUser();
    post("/auth/register", json(req), null);

    ResponseEntity<String> res =
        post("/auth/login", json(new LoginRequest(req.getEmail(), req.getPassword())), null);

    assertEquals(HttpStatus.OK, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    String token = body.get("token").asText();
    assertEquals("Bearer", body.get("type").asText());
    assertEquals(3, token.split("\\.").length);

    Claims claims =
        Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token).getPayload();
    assertEquals(req.getEmail(), claims.getSubject());
    assertEquals("ROLE_" + req.getRole(), claims.get("role", String.class));
  }

  @Test
  @SneakyThrows
  void login_wrongPassword_unauthorized() {
    UserRequest req = randomUser();
    post("/auth/register", json(req), null);

    ResponseEntity<String> res =
        post("/auth/login", json(new LoginRequest(req.getEmail(), "wrong-password")), null);

    assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void login_unknownEmail_unauthorized() {
    ResponseEntity<String> res =
        post("/auth/login", json(new LoginRequest("nobody-" + uuid() + "@test.com", "pw")), null);

    assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void login_blankEmail_badRequest() {
    ResponseEntity<String> res = post("/auth/login", json(new LoginRequest("", "pw")), null);

    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.get("details").has("email"));
  }

  @Test
  @SneakyThrows
  void login_invalidEmail_badRequest() {
    ResponseEntity<String> res =
        post("/auth/login", json(new LoginRequest("not-an-email", "pw")), null);

    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.get("details").has("email"));
  }

  @Test
  @SneakyThrows
  void login_blankPassword_badRequest() {
    ResponseEntity<String> res =
        post("/auth/login", json(new LoginRequest("someone-" + uuid() + "@test.com", "")), null);

    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    JsonNode body = objectMapper.readTree(res.getBody());
    assertTrue(body.get("details").has("password"));
  }

  @Test
  @SneakyThrows
  void login_withValidBearerToken_forbidden() {
    UserRequest req = randomUser();
    String token = registerAndLogin(req);

    ResponseEntity<String> res =
        post("/auth/login", json(new LoginRequest(req.getEmail(), req.getPassword())), token);

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void ping_withoutToken_ok() {
    ResponseEntity<String> res = get("/ping", null);

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals("pong", res.getBody());
  }

  @Test
  @SneakyThrows
  void ping_withValidBearerToken_forbidden() {
    UserRequest req = randomUser();
    String token = registerAndLogin(req);

    ResponseEntity<String> res = get("/ping", token);

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void ping_withMalformedBearerToken_ok() {
    ResponseEntity<String> res = get("/ping", "not.a.valid.jwt");

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals("pong", res.getBody());
  }

  @Test
  @SneakyThrows
  void ping_withExpiredBearerToken_ok() {
    String expiredToken = expiredToken("expired-" + uuid() + "@test.com");

    ResponseEntity<String> res = get("/ping", expiredToken);

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals("pong", res.getBody());
  }

  @Test
  @SneakyThrows
  void unknownPath_forbidden() {
    ResponseEntity<String> res = get("/this-route-does-not-exist", null);

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  private String registerAndLogin(UserRequest req) throws Exception {
    post("/auth/register", json(req), null);
    ResponseEntity<String> res =
        post("/auth/login", json(new LoginRequest(req.getEmail(), req.getPassword())), null);
    assertEquals(HttpStatus.OK, res.getStatusCode());
    return objectMapper.readTree(res.getBody()).get("token").asText();
  }

  private String expiredToken(String email) {
    return Jwts.builder()
        .subject(email)
        .claim("role", "ROLE_STUDENT")
        .issuedAt(new Date(System.currentTimeMillis() - 120_000L))
        .expiration(new Date(System.currentTimeMillis() - 60_000L))
        .signWith(signingKey())
        .compact();
  }

  private SecretKey signingKey() {
    return Keys.hmacShaKeyFor(
        Base64.getDecoder().decode(environment.getRequiredProperty("app.jwt.secret")));
  }

  private UserRequest randomUser() {
    String suffix = uuid();
    return UserRequest.builder()
        .ref("ref-" + suffix)
        .firstname("John")
        .lastname("Doe")
        .email("john-" + suffix + "@test.com")
        .password("p@ssw0rd-" + suffix)
        .role(Role.STUDENT)
        .build();
  }

  private String uuid() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
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

  private ResponseEntity<String> get(String path, String bearerToken) {
    HttpHeaders headers = new HttpHeaders();
    if (bearerToken != null) {
      headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
    }
    HttpEntity<Void> entity = new HttpEntity<>(headers);
    return send(() -> restTemplate.exchange(url(path), HttpMethod.GET, entity, String.class));
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
