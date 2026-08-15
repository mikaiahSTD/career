package com.rmm.std.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rmm.std.constant.Role;
import com.rmm.std.domain.User;
import com.rmm.std.endpoint.event.EventProducer;
import com.rmm.std.endpoint.event.model.SendTranscriptEmailRequested;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.repository.UserRepository;
import com.rmm.std.repository.model.JUser;
import com.rmm.std.security.UserPrincipal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TranscriptServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private EventProducer<SendTranscriptEmailRequested> eventProducer;

  private TranscriptService service;

  @BeforeEach
  void setUp() {
    service = new TranscriptService(userRepository, eventProducer);
  }

  @Test
  void send_existingStudent_producesEventAndReturnsAccepted() {
    var studentId = UUID.randomUUID();

    var securityUser = User.builder().id(studentId).role(Role.STUDENT).build();
    var principal = UserPrincipal.create(securityUser);
    var repositoryUser = JUser.builder().id(studentId).build();

    when(userRepository.findById(studentId)).thenReturn(Optional.of(repositoryUser));

    var res = service.send(studentId, null, principal);

    assertEquals(
        "Transcript generation requested. An email will be sent with a download link.",
        res.getMessage());
    assertNull(res.getS3Url());

    ArgumentCaptor<List<SendTranscriptEmailRequested>> captor = ArgumentCaptor.forClass(List.class);
    verify(eventProducer).accept(captor.capture());
    var event = captor.getValue().get(0);
    assertEquals(studentId, event.getStudentId());
    assertNull(event.getSemesterId());
  }

  @Test
  void send_unknownStudent_throwsNotFound() {
    var studentId = UUID.randomUUID();
    when(userRepository.findById(studentId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.send(studentId, null, null));
  }
}
