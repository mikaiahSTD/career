package com.rmm.std.conf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.rmm.std.constant.Role;
import com.rmm.std.repository.UserRepository;
import com.rmm.std.repository.model.JUser;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class BootstrapAdminTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;

  @Test
  void disabled_doesNothing() {
    BootstrapAdmin runner = runner(false, "a@test.com");

    runner.run(null);

    verifyNoInteractions(userRepository, passwordEncoder);
  }

  @Test
  void missingCredentials_skips() {
    BootstrapAdmin runner = runner(true, "");

    runner.run(null);

    verifyNoInteractions(userRepository, passwordEncoder);
  }

  @Test
  void adminAlreadyExists_skips() {
    when(userRepository.findByRole(Role.ADMIN))
        .thenReturn(List.of(JUser.builder().email("existing@test.com").build()));

    BootstrapAdmin runner = runner(true, "a@test.com");
    runner.run(null);

    verify(userRepository, never()).save(any());
  }

  @Test
  void emailAlreadyTaken_skips() {
    when(userRepository.findByRole(Role.ADMIN)).thenReturn(List.of());
    when(userRepository.existsByEmail("a@test.com")).thenReturn(true);

    BootstrapAdmin runner = runner(true, "a@test.com");
    runner.run(null);

    verify(userRepository, never()).save(any());
  }

  @Test
  void createsAdmin_whenNoneExists() {
    when(userRepository.findByRole(Role.ADMIN)).thenReturn(List.of());
    when(userRepository.existsByEmail("a@test.com")).thenReturn(false);
    when(passwordEncoder.encode("s3cr3t")).thenReturn("encoded");

    BootstrapAdmin runner = runner(true, "a@test.com");
    runner.run(null);

    ArgumentCaptor<JUser> captor = ArgumentCaptor.forClass(JUser.class);
    verify(userRepository).save(captor.capture());
    JUser saved = captor.getValue();
    assertEquals(Role.ADMIN, saved.getRole());
    assertEquals("a@test.com", saved.getEmail());
    assertEquals("ADMIN-BOOTSTRAP", saved.getRef());
    assertEquals("encoded", saved.getPassword());
  }

  private BootstrapAdmin runner(boolean enabled, String email) {
    return new BootstrapAdmin(
        userRepository,
        passwordEncoder,
        enabled,
        "ADMIN-BOOTSTRAP",
        email,
        "Administrator",
        "System",
        "s3cr3t");
  }
}
