package com.rmm.std.endpoint.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.rmm.std.constant.Role;
import com.rmm.std.endpoint.event.EventProducer;
import com.rmm.std.endpoint.event.model.GraduatesExportRequested;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GraduatesExportIT extends AbstractControllerIT {

  @MockBean private EventProducer<GraduatesExportRequested> eventProducer;

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
