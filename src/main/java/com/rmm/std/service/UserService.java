package com.rmm.std.service;

import com.rmm.std.domain.User;
import com.rmm.std.dto.UserRequest;
import com.rmm.std.dto.UserResponse;
import com.rmm.std.exception.ConflictException;
import com.rmm.std.mapper.UserMapper;
import com.rmm.std.repository.UserRepository;
import com.rmm.std.repository.model.JUser;
import com.rmm.std.security.UserPrincipal;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
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

    JUser jUser = userMapper.toJUser(user);
    userRepository.save(jUser);

    UserResponse res = userMapper.toRes(user);
    return res;
  }
}
