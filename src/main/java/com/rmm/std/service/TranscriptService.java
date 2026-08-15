package com.rmm.std.service;

import com.rmm.std.constant.Role;
import com.rmm.std.dto.TranscriptSendResponse;
import com.rmm.std.endpoint.event.EventProducer;
import com.rmm.std.endpoint.event.model.SendTranscriptEmailRequested;
import com.rmm.std.exception.ForbiddenException;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.repository.UserRepository;
import com.rmm.std.security.UserPrincipal;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TranscriptService {

  private final UserRepository userRepository;
  private final EventProducer<SendTranscriptEmailRequested> eventProducer;

  public TranscriptSendResponse send(UUID studentId, UUID semesterId, UserPrincipal principal) {
    userRepository
        .findById(studentId)
        .orElseThrow(() -> new NotFoundException("Student not found: " + studentId));

    if (principal.getUser().getRole() != Role.ADMIN
        && !principal.getUser().getId().equals(studentId)) {
      throw new ForbiddenException("You can only send your own transcript");
    }

    var event =
        SendTranscriptEmailRequested.builder().studentId(studentId).semesterId(semesterId).build();
    eventProducer.accept(List.of(event));

    return TranscriptSendResponse.builder()
        .message("Transcript generation requested. An email will be sent with a download link.")
        .build();
  }
}
