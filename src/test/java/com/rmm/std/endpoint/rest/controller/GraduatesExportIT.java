package com.rmm.std.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rmm.std.constant.Role;
import com.rmm.std.endpoint.event.EventProducer;
import com.rmm.std.endpoint.event.model.GraduatesExportRequested;
import com.rmm.std.file.bucket.BucketComponent;
import com.rmm.std.file.hash.FileHash;
import com.rmm.std.service.event.GraduatesExportRequestedService;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GraduatesExportIT extends AbstractControllerIT {

  @MockBean private EventProducer<GraduatesExportRequested> eventProducer;

  @MockBean private BucketComponent bucketComponent;

  @Autowired private GraduatesExportRequestedService graduatesExportRequestedService;

  private final Map<String, File> bucketStore = new HashMap<>();

  @BeforeEach
  void setUpBucketMock() {
    bucketStore.clear();
    when(bucketComponent.upload(any(File.class), anyString()))
        .thenAnswer(
            inv -> {
              bucketStore.put(inv.getArgument(1), inv.getArgument(0));
              return new FileHash(null, null);
            });
    when(bucketComponent.download(anyString()))
        .thenAnswer(
            inv -> {
              File file = bucketStore.get(inv.getArgument(0));
              if (file == null) {
                throw new RuntimeException("not found");
              }
              return file;
            });
  }

  @Test
  @SneakyThrows
  void requestThenWorkerThenDownload_returnsXlsxWithGraduate() {
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

    String courseId = createCourse(admin.token());
    String examId = createExam(admin.token(), courseId);
    createGrade(admin.token(), examId, student.id().toString());

    ResponseEntity<String> request =
        post("/promotions/" + promotionId + "/graduates/export", null, admin.token());
    assertEquals(HttpStatus.ACCEPTED, request.getStatusCode());

    ArgumentCaptor<List<GraduatesExportRequested>> captor = ArgumentCaptor.forClass(List.class);
    verify(eventProducer).accept(captor.capture());
    GraduatesExportRequested event = captor.getValue().get(0);
    assertEquals(promotionId, event.getPromotionId().toString());

    graduatesExportRequestedService.accept(event);

    verify(bucketComponent)
        .upload(any(File.class), eq("graduates-exports/" + promotionId + "/graduates.xlsx"));

    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + admin.token());
    ResponseEntity<byte[]> download =
        restTemplate.exchange(
            url("/promotions/" + promotionId + "/graduates/export"),
            HttpMethod.GET,
            new HttpEntity<>(headers),
            byte[].class);

    assertEquals(HttpStatus.OK, download.getStatusCode());
    assertNotNull(download.getBody());
    assertTrue(download.getBody().length > 0);
    assertEquals('P', download.getBody()[0]);
    assertEquals('K', download.getBody()[1]);
  }

  @Test
  @SneakyThrows
  void requestExport_graduatedPromotion_returns202AndProducesEvent() {
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

    ResponseEntity<String> res =
        post("/promotions/" + promotionId + "/graduates/export", null, admin.token());

    assertEquals(HttpStatus.ACCEPTED, res.getStatusCode());
    verify(eventProducer).accept(any());
  }

  @Test
  @SneakyThrows
  void requestExport_withoutGraduatedStudent_returns202() {
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());

    ResponseEntity<String> res =
        post("/promotions/" + promotionId + "/graduates/export", null, admin.token());

    assertEquals(HttpStatus.ACCEPTED, res.getStatusCode());
    verify(eventProducer).accept(any());
  }

  @Test
  @SneakyThrows
  void requestExport_unknownPromotion_notFound() {
    var admin = registerAndLogin(Role.ADMIN);

    ResponseEntity<String> res =
        post("/promotions/" + UUID.randomUUID() + "/graduates/export", null, admin.token());

    assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
  }

  @Test
  @SneakyThrows
  void requestExport_asStudent_forbidden() {
    var student = registerAndLogin(Role.STUDENT);
    var admin = registerAndLogin(Role.ADMIN);
    String promotionId = createPromotion(admin.token());

    ResponseEntity<String> res =
        post("/promotions/" + promotionId + "/graduates/export", null, student.token());

    assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
  }
}
