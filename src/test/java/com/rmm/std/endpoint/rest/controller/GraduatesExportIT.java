package com.rmm.std.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rmm.std.constant.Role;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

class GraduatesExportIT extends AbstractControllerIT {

  private final RestTemplate byteRestTemplate = new RestTemplate();

  @Test
  @SneakyThrows
  void export_graduates_withGraduatedStudent_returnsXlsx() {
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());
    var student = registerAndLogin(Role.STUDENT);
    post(
        "/user-promotions",
        json(
            "{\"userId\":\""
                + student.id()
                + "\",\"promotionId\":\""
                + promotionId
                + "\",\"status\":\"GRADUATED\"}"),
        admin.token());

    ResponseEntity<byte[]> res =
        getBytes("/promotions/" + promotionId + "/graduates/export", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    MediaType contentType = res.getHeaders().getContentType();
    assertTrue(contentType != null && contentType.toString().contains("spreadsheetml"));
    assertTrue(res.getBody().length > 0);
  }

  @Test
  @SneakyThrows
  void export_graduates_withoutGraduatedStudent_returnsXlsxWithHeader() {
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());

    ResponseEntity<byte[]> res =
        getBytes("/promotions/" + promotionId + "/graduates/export", admin.token());

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertTrue(res.getBody().length > 0);
  }

  @Test
  @SneakyThrows
  void export_unknownPromotion_notFound() {
    var admin = registerAndLogin(Role.ADMIN);

    ResponseEntity<byte[]> res =
        getBytes("/promotions/" + UUID.randomUUID() + "/graduates/export", admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void export_asStudent_forbidden() {
    var student = registerAndLogin(Role.STUDENT);
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());

    ResponseEntity<byte[]> res =
        getBytes("/promotions/" + promotionId + "/graduates/export", student.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }

  private ResponseEntity<byte[]> getBytes(String path, String bearerToken) {
    HttpHeaders headers = new HttpHeaders();
    if (bearerToken != null) {
      headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
    }
    HttpEntity<Void> entity = new HttpEntity<>(headers);
    try {
      return byteRestTemplate.exchange(url(path), HttpMethod.GET, entity, byte[].class);
    } catch (HttpClientErrorException e) {
      return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
    }
  }
}
