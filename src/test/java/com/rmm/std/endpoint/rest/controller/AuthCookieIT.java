package com.rmm.std.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rmm.std.constant.Role;
import com.rmm.std.dto.LoginRequest;
import com.rmm.std.dto.UserRequest;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

class AuthCookieIT extends AbstractControllerIT {

  private static final String JWT_COOKIE = "jwt_token";

  @Test
  @SneakyThrows
  void login_setsJwtCookie() {
    String cookie = loginAndExtractCookie();

    assertNotNull(cookie);
  }

  @Test
  @SneakyThrows
  void jwtCookieAuthenticatesMeEndpoint() {
    String cookie = loginAndExtractCookie();

    ResponseEntity<String> res = getWithCookie("/auth/me", cookie);

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertTrue(res.getBody().contains("\"role\":\"ADMIN\""));
  }

  @Test
  @SneakyThrows
  void jwtCookieAuthenticatesAdminUiPage() {
    String cookie = loginAndExtractCookie();

    ResponseEntity<String> res = getWithCookie("/ui/promotions", cookie);

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertTrue(res.getBody().contains("Promotions"));
  }

  @Test
  @SneakyThrows
  void meWithoutCredentials_forbidden() {
    ResponseEntity<String> res = getWithCookie("/auth/me", "");

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void logout_clearsJwtCookie() {
    String cookie = loginAndExtractCookie();

    ResponseEntity<String> res = postWithCookie("/auth/logout", cookie);

    assertEquals(HttpStatus.OK, res.getStatusCode());
    String joined = String.join("; ", res.getHeaders().get(HttpHeaders.SET_COOKIE));
    assertTrue(joined.contains(JWT_COOKIE + "="));
    assertTrue(joined.contains("Max-Age=0"));
  }

  @Test
  @SneakyThrows
  void logout_revokesJwtCookieToken() {
    String cookie = loginAndExtractCookie();

    ResponseEntity<String> logout = postWithCookie("/auth/logout", cookie);
    assertEquals(HttpStatus.OK, logout.getStatusCode());

    ResponseEntity<String> me = getWithCookie("/auth/me", cookie);
    assertEquals(HttpStatus.FORBIDDEN, me.getStatusCode());
  }

  @Test
  @SneakyThrows
  void logout_revokesBearerToken() {
    UserRequest user = randomUser(Role.ADMIN);
    assertEquals(HttpStatus.OK, post("/auth/register", json(user), null).getStatusCode());

    ResponseEntity<String> login =
        post("/auth/login", json(new LoginRequest(user.getEmail(), user.getPassword())), null);
    assertEquals(HttpStatus.OK, login.getStatusCode());
    String token = objectMapper.readTree(login.getBody()).get("token").asText();

    assertEquals(HttpStatus.OK, get("/users", token).getStatusCode());

    assertEquals(HttpStatus.OK, post("/auth/logout", "{}", token).getStatusCode());

    assertEquals(HttpStatus.FORBIDDEN, get("/users", token).getStatusCode());
  }

  private String loginAndExtractCookie() throws Exception {
    UserRequest user = randomUser(Role.ADMIN);
    assertEquals(HttpStatus.OK, post("/auth/register", json(user), null).getStatusCode());

    ResponseEntity<String> login =
        post("/auth/login", json(new LoginRequest(user.getEmail(), user.getPassword())), null);
    assertEquals(HttpStatus.OK, login.getStatusCode());

    String joined = joinSetCookies(login);
    for (String part : joined.split(";")) {
      String trimmed = part.trim();
      if (trimmed.startsWith(JWT_COOKIE + "=")) {
        return trimmed.substring((JWT_COOKIE + "=").length());
      }
    }
    throw new IllegalStateException("No " + JWT_COOKIE + " cookie in: " + joined);
  }

  private String joinSetCookies(ResponseEntity<String> res) {
    List<String> setCookies = res.getHeaders().get(HttpHeaders.SET_COOKIE);
    assertNotNull(setCookies);
    return String.join("; ", setCookies);
  }

  private ResponseEntity<String> getWithCookie(String path, String cookie) {
    HttpHeaders headers = new HttpHeaders();
    if (!cookie.isEmpty()) {
      headers.add(HttpHeaders.COOKIE, JWT_COOKIE + "=" + cookie);
    }
    HttpEntity<Void> entity = new HttpEntity<>(headers);
    return send(() -> restTemplate.exchange(url(path), HttpMethod.GET, entity, String.class));
  }

  private ResponseEntity<String> postWithCookie(String path, String cookie) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add(HttpHeaders.COOKIE, JWT_COOKIE + "=" + cookie);
    HttpEntity<Void> entity = new HttpEntity<>(headers);
    return send(() -> restTemplate.exchange(url(path), HttpMethod.POST, entity, String.class));
  }
}
