package com.rmm.std.endpoint.rest.controller;

import com.rmm.std.dto.TranscriptSendRequest;
import com.rmm.std.dto.TranscriptSendResponse;
import com.rmm.std.security.UserPrincipal;
import com.rmm.std.service.TranscriptService;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class TranscriptController {

  private final TranscriptService transcriptService;

  @PostMapping("/students/{studentId}/transcript/send")
  public ResponseEntity<TranscriptSendResponse> send(
      @PathVariable UUID studentId,
      @RequestBody(required = false) TranscriptSendRequest req,
      @AuthenticationPrincipal UserPrincipal principal) {
    UUID semesterId = req == null ? null : req.getSemesterId();
    return ResponseEntity.accepted().body(transcriptService.send(studentId, semesterId, principal));
  }
}
