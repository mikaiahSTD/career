package com.rmm.std.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rmm.std.constant.Role;
import com.rmm.std.dto.UserRequest;
import com.rmm.std.dto.UserResponse;
import com.rmm.std.exception.ConflictException;
import com.rmm.std.mapper.UserMapper;
import com.rmm.std.repository.GradeRepository;
import com.rmm.std.repository.SemesterRepository;
import com.rmm.std.repository.UserRepository;
import com.rmm.std.repository.model.JUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private GradeRepository gradeRepository;
  @Mock private com.rmm.std.mapper.GradeMapper gradeMapper;
  @Mock private SemesterRepository semesterRepository;

  private final UserMapper userMapper = new UserMapper();

  private UserService service;

  @BeforeEach
  void setUp() {
    service =
        new UserService(
            userRepository,
            passwordEncoder,
            userMapper,
            gradeRepository,
            gradeMapper,
            semesterRepository);
  }

  @Test
  void registerStudent_forcesStudentRole_ignoringRequestedRole() {
    UserRequest req = request(Role.ADMIN);
    when(userRepository.existsByEmail(req.getEmail())).thenReturn(false);
    when(passwordEncoder.encode(req.getPassword())).thenReturn("encoded");
    when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    UserResponse res = service.registerStudent(req);

    ArgumentCaptor<JUser> captor = ArgumentCaptor.forClass(JUser.class);
    verify(userRepository).save(captor.capture());
    assertEquals(Role.STUDENT, captor.getValue().getRole());
    assertEquals(Role.STUDENT, res.getRole());
  }

  @Test
  void registerStudent_duplicateEmail_conflict() {
    UserRequest req = request(Role.STUDENT);
    when(userRepository.existsByEmail(req.getEmail())).thenReturn(true);

    assertThrows(ConflictException.class, () -> service.registerStudent(req));
  }

  @Test
  void createUser_keepsRequestedRole() {
    UserRequest req = request(Role.ADMIN);
    when(userRepository.existsByEmail(req.getEmail())).thenReturn(false);
    when(passwordEncoder.encode(req.getPassword())).thenReturn("encoded");
    when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.createUser(req);

    ArgumentCaptor<JUser> captor = ArgumentCaptor.forClass(JUser.class);
    verify(userRepository).save(captor.capture());
    assertEquals(Role.ADMIN, captor.getValue().getRole());
  }

  private UserRequest request(Role role) {
    return UserRequest.builder()
        .ref("REF-1")
        .firstname("John")
        .lastname("Doe")
        .email("john@test.com")
        .password("Secret123")
        .role(role)
        .build();
  }
}
