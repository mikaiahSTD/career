package com.rmm.std.service;

import com.rmm.std.constant.Role;
import com.rmm.std.domain.User;
import com.rmm.std.dto.GradeResponse;
import com.rmm.std.dto.PageResponse;
import com.rmm.std.dto.UserRequest;
import com.rmm.std.dto.UserResponse;
import com.rmm.std.exception.ConflictException;
import com.rmm.std.exception.NotFoundException;
import com.rmm.std.mapper.GradeMapper;
import com.rmm.std.mapper.UserMapper;
import com.rmm.std.repository.GradeRepository;
import com.rmm.std.repository.SemesterRepository;
import com.rmm.std.repository.UserRepository;
import com.rmm.std.repository.model.JUser;
import com.rmm.std.security.UserPrincipal;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {
  private UserRepository userRepository;
  private PasswordEncoder passwordEncoder;
  private UserMapper userMapper;
  private GradeRepository gradeRepository;
  private GradeMapper gradeMapper;
  private SemesterRepository semesterRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    JUser jUser =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + email));
    User user = userMapper.toUser(jUser);
    return UserPrincipal.create(user);
  }

  @Transactional
  public UserResponse createUser(UserRequest req) {
    if (userRepository.existsByEmail(req.getEmail())) {
      throw new ConflictException("User with email: " + req.getEmail() + " already exists");
    }
    User user = userMapper.toUser(req);
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    JUser jUser = userRepository.save(userMapper.toJUser(user));
    UserResponse res = userMapper.toRes(jUser);
    return res;
  }

  @Transactional
  public UserResponse registerStudent(UserRequest req) {
    req.setRole(Role.STUDENT);
    return createUser(req);
  }

  public PageResponse<UserResponse> list(Role role, Pageable pageable) {
    Page<JUser> page =
        role == null ? userRepository.findAll(pageable) : userRepository.findByRole(role, pageable);
    return PageResponse.from(page, page.map(userMapper::toRes).toList());
  }

  public UserResponse get(UUID id) {
    return userMapper.toRes(getEntity(id));
  }

  @Transactional
  public UserResponse update(UUID id, UserRequest req) {
    JUser existing = getEntity(id);
    if (userRepository.existsByEmail(req.getEmail())
        && !existing.getEmail().equals(req.getEmail())) {
      throw new ConflictException("User with email: " + req.getEmail() + " already exists");
    }
    existing.setRef(req.getRef());
    existing.setFirstname(req.getFirstname());
    existing.setLastname(req.getLastname());
    existing.setEmail(req.getEmail());
    existing.setPassword(passwordEncoder.encode(req.getPassword()));
    existing.setRole(req.getRole());
    return userMapper.toRes(userRepository.save(existing));
  }

  @Transactional
  public void delete(UUID id) {
    getEntity(id);
    userRepository.deleteById(id);
  }

  public List<GradeResponse> getMyGrades(UUID studentId, UUID semesterId) {
    if (semesterId == null) {
      return gradeRepository.findByStudentId(studentId).stream().map(gradeMapper::toRes).toList();
    }
    Integer semesterNumber =
        semesterRepository
            .findById(semesterId)
            .orElseThrow(() -> new NotFoundException("Semester not found: " + semesterId))
            .getNumber();
    return gradeRepository.findGradesForStudentInSemester(studentId, semesterNumber).stream()
        .map(gradeMapper::toRes)
        .toList();
  }

  private JUser getEntity(UUID id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("User not found: " + id));
  }

  public User findByEmail(String email) {
    JUser jUser =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    return userMapper.toUser(jUser);
  }
}
