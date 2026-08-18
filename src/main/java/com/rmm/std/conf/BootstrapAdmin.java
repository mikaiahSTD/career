package com.rmm.std.conf;

import com.rmm.std.constant.Role;
import com.rmm.std.repository.UserRepository;
import com.rmm.std.repository.model.JUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class BootstrapAdmin implements ApplicationRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final boolean enabled;
  private final String ref;
  private final String email;
  private final String firstname;
  private final String lastname;
  private final String password;

  public BootstrapAdmin(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      @Value("${app.bootstrap-admin.enabled}") boolean enabled,
      @Value("${app.bootstrap-admin.ref}") String ref,
      @Value("${app.bootstrap-admin.email}") String email,
      @Value("${app.bootstrap-admin.firstname}") String firstname,
      @Value("${app.bootstrap-admin.lastname}") String lastname,
      @Value("${app.bootstrap-admin.password}") String password) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.enabled = enabled;
    this.ref = ref;
    this.email = email;
    this.firstname = firstname;
    this.lastname = lastname;
    this.password = password;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (!enabled) {
      return;
    }
    if (email.isBlank() || password.isBlank()) {
      log.warn(
          "Bootstrap admin is enabled but 'app.bootstrap-admin.email' and/or "
              + "'app.bootstrap-admin.password' are not set; skipping");
      return;
    }
    if (!userRepository.findByRole(Role.ADMIN).isEmpty()) {
      return;
    }
    if (userRepository.existsByEmail(email)) {
      log.warn("Bootstrap admin email '{}' already belongs to a user; skipping", email);
      return;
    }
    userRepository.save(
        JUser.builder()
            .ref(ref)
            .firstname(firstname)
            .lastname(lastname)
            .email(email)
            .password(passwordEncoder.encode(password))
            .role(Role.ADMIN)
            .build());
    log.info("Bootstrap admin created with email '{}'", email);
  }
}
