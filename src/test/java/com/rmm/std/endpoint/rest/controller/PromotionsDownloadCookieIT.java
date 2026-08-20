package com.rmm.std.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.rmm.std.conf.EnvConf;
import com.rmm.std.constant.Role;
import com.rmm.std.dto.LoginRequest;
import com.rmm.std.dto.UserRequest;
import com.rmm.std.endpoint.event.EventProducer;
import com.rmm.std.endpoint.event.model.GraduatesExportRequested;
import java.net.CookieManager;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

class PromotionsDownloadCookieIT extends AbstractControllerIT {

  private static final String JWT_COOKIE = "jwt_token";
  private static final String XSRF_COOKIE = "XSRF-TOKEN";
  private static final String XSRF_HEADER = "X-XSRF-TOKEN";

  @MockBean private EventProducer<GraduatesExportRequested> eventProducer;

  @Test
  @SneakyThrows
  void download_withCookieAuthAndCsrfToken_returns202() {
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());
    String jwtCookie = loginAndExtractCookie(EnvConf.ADMIN_EMAIL, EnvConf.ADMIN_PASSWORD);

    ResponseEntity<String> pageRes = getWithCookies("/ui/promotions", jwtCookie, null);
    assertEquals(HttpStatus.OK, pageRes.getStatusCode());

    String csrfToken = metaToken(pageRes.getBody());
    assertNotNull(csrfToken, "Expected _csrf token rendered by the UI page");

    ResponseEntity<String> res =
        postWithCookies(
            "/promotions/" + promotionId + "/graduates/export", jwtCookie, null, csrfToken);

    assertEquals(HttpStatus.ACCEPTED, res.getStatusCode());
    verify(eventProducer).accept(any());
  }

  @Test
  @SneakyThrows
  void download_withBrowserLikeCookieSession_returns202() {
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());

    CookieManager cookieManager = new CookieManager();
    RestTemplate browser = browserTemplate(cookieManager);

    ResponseEntity<String> login =
        postWithCookieJar(
            browser,
            "/auth/login",
            json(new LoginRequest(EnvConf.ADMIN_EMAIL, EnvConf.ADMIN_PASSWORD)));
    assertEquals(HttpStatus.OK, login.getStatusCode());

    ResponseEntity<String> pageRes = getWithCookieJar(browser, "/ui/promotions");
    assertEquals(HttpStatus.OK, pageRes.getStatusCode());
    String csrfToken = metaToken(pageRes.getBody());
    assertNotNull(csrfToken, "Expected _csrf token rendered by the UI page");

    HttpHeaders headers = new HttpHeaders();
    headers.set(XSRF_HEADER, csrfToken);
    HttpEntity<Void> entity = new HttpEntity<>(headers);
    ResponseEntity<String> res =
        send(
            () ->
                browser.exchange(
                    url("/promotions/" + promotionId + "/graduates/export"),
                    HttpMethod.POST,
                    entity,
                    String.class));

    assertEquals(HttpStatus.ACCEPTED, res.getStatusCode());
    verify(eventProducer).accept(any());
  }

  @Test
  @SneakyThrows
  void download_withoutCsrfToken_forbidden() {
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());
    String jwtCookie = loginAndExtractCookie(EnvConf.ADMIN_EMAIL, EnvConf.ADMIN_PASSWORD);

    ResponseEntity<String> res =
        postWithCookies("/promotions/" + promotionId + "/graduates/export", jwtCookie, null, null);

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void download_withoutAuthentication_forbidden() {
    ResponseEntity<String> res =
        postWithCookies("/promotions/" + UUID.randomUUID() + "/graduates/export", null, null, null);

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void download_withStudentCookie_forbidden() {
    String studentCookie = registerStudentAndGetCookie();
    String csrfToken = metaFromPage(studentCookie);

    ResponseEntity<String> res =
        postWithCookies(
            "/promotions/" + UUID.randomUUID() + "/graduates/export",
            studentCookie,
            null,
            csrfToken);

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void download_withTeacherCookie_forbidden() {
    String teacherCookie = createTeacherAndGetCookie();
    String csrfToken = metaFromPage(teacherCookie);

    ResponseEntity<String> res =
        postWithCookies(
            "/promotions/" + UUID.randomUUID() + "/graduates/export",
            teacherCookie,
            null,
            csrfToken);

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void download_getWhenNotReady_returns404() {
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());

    ResponseEntity<String> res =
        get("/promotions/" + promotionId + "/graduates/export", admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @SneakyThrows
  private String registerStudentAndGetCookie() {
    UserRequest req = randomUser(Role.STUDENT);
    post("/auth/register", json(req), null);
    return loginAndExtractCookie(req.getEmail(), req.getPassword());
  }

  @SneakyThrows
  private String createTeacherAndGetCookie() {
    Registration admin = registerAndLogin(Role.ADMIN);
    UserRequest req = randomUser(Role.TEACHER);
    post("/users", json(req), admin.token());
    return loginAndExtractCookie(req.getEmail(), req.getPassword());
  }

  @SneakyThrows
  private String metaFromPage(String jwtCookie) {
    ResponseEntity<String> pageRes = getWithCookies("/ui", jwtCookie, null);
    String csrfToken = metaToken(pageRes.getBody());
    assertNotNull(csrfToken, "Expected _csrf token rendered by a UI page");
    return csrfToken;
  }

  private String metaToken(String body) {
    if (body == null) {
      return null;
    }
    var matcher =
        java.util.regex.Pattern.compile("meta\\s+name=\"_csrf\"\\s+content=\"([^\"]*)\"")
            .matcher(body);
    return matcher.find() ? matcher.group(1) : null;
  }

  @SneakyThrows
  private String loginAndExtractCookie(String email, String password) {
    ResponseEntity<String> login =
        post("/auth/login", json(new LoginRequest(email, password)), null);
    assertEquals(HttpStatus.OK, login.getStatusCode());
    String cookie = extractCookie(login, JWT_COOKIE);
    assertNotNull(cookie, "Expected jwt_token cookie after login");
    return cookie;
  }

  private RestTemplate browserTemplate(CookieManager cookieManager) {
    HttpClient client = HttpClient.newBuilder().cookieHandler(cookieManager).build();
    return new RestTemplate(new JdkClientHttpRequestFactory(client));
  }

  private ResponseEntity<String> postWithCookieJar(RestTemplate browser, String path, String body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<>(body, headers);
    return send(() -> browser.exchange(url(path), HttpMethod.POST, entity, String.class));
  }

  private ResponseEntity<String> getWithCookieJar(RestTemplate browser, String path) {
    HttpEntity<Void> entity = new HttpEntity<>(new HttpHeaders());
    return send(() -> browser.exchange(url(path), HttpMethod.GET, entity, String.class));
  }

  private String extractCookie(ResponseEntity<String> res, String name) {
    List<String> setCookies = res.getHeaders().get(HttpHeaders.SET_COOKIE);
    if (setCookies == null) {
      return null;
    }
    for (String setCookie : setCookies) {
      String first = setCookie.split(";")[0].trim();
      int eq = first.indexOf('=');
      if (eq > 0 && first.substring(0, eq).equals(name)) {
        return first.substring(eq + 1);
      }
    }
    return null;
  }

  private ResponseEntity<String> getWithCookies(String path, String jwtCookie, String xsrfCookie) {
    HttpHeaders headers = new HttpHeaders();
    addCookies(headers, jwtCookie, xsrfCookie);
    HttpEntity<Void> entity = new HttpEntity<>(headers);
    return send(() -> restTemplate.exchange(url(path), HttpMethod.GET, entity, String.class));
  }

  private ResponseEntity<String> postWithCookies(
      String path, String jwtCookie, String xsrfCookie, String xsrfHeader) {
    HttpHeaders headers = new HttpHeaders();
    addCookies(headers, jwtCookie, xsrfCookie);
    if (xsrfHeader != null) {
      headers.set(XSRF_HEADER, xsrfHeader);
    }
    HttpEntity<Void> entity = new HttpEntity<>(headers);
    return send(() -> restTemplate.exchange(url(path), HttpMethod.POST, entity, String.class));
  }

  private void addCookies(HttpHeaders headers, String jwtCookie, String xsrfCookie) {
    List<String> cookies = new ArrayList<>();
    if (jwtCookie != null) {
      cookies.add(JWT_COOKIE + "=" + jwtCookie);
    }
    if (xsrfCookie != null) {
      cookies.add(XSRF_COOKIE + "=" + xsrfCookie);
    }
    if (!cookies.isEmpty()) {
      headers.add(HttpHeaders.COOKIE, String.join("; ", cookies));
    }
  }
}
