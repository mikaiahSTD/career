package com.rmm.std.mapper;

import com.rmm.std.domain.User;
import com.rmm.std.dto.UserRequest;
import com.rmm.std.dto.UserResponse;
import com.rmm.std.repository.model.JUser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserMapper {

  public JUser toJUser(User user) {
    if (user == null) {
      return null;
    }
    return JUser.builder()
        .id(user.getId())
        .firstname(user.getFirstname())
        .lastname(user.getLastname())
        .email(user.getEmail())
        .password(user.getPassword())
        .role(user.getRole())
        .ref(user.getRef())
        .build();
  }

  public User toUser(JUser jUser) {
    if (jUser == null) {
      return null;
    }
    return User.builder()
        .id(jUser.getId())
        .firstname(jUser.getFirstname())
        .lastname(jUser.getLastname())
        .email(jUser.getEmail())
        .password(jUser.getPassword())
        .role(jUser.getRole())
        .ref(jUser.getRef())
        .build();
  }

  public User toUser(UserRequest req) {
    if (req == null) {
      return null;
    }
    return User.builder()
        .firstname(req.getFirstname())
        .lastname(req.getLastname())
        .email(req.getEmail())
        .password(req.getPassword())
        .role(req.getRole())
        .ref(req.getRef())
        .build();
  }

  public UserResponse toRes(User user) {
    if (user == null) {
      return null;
    }
    return UserResponse.builder()
        .id(user.getId())
        .firstname(user.getFirstname())
        .lastname(user.getLastname())
        .email(user.getEmail())
        .role(user.getRole())
        .ref(user.getRef())
        .build();
  }

  public UserResponse toRes(JUser jUser) {
    if (jUser == null) {
      return null;
    }
    return UserResponse.builder()
        .id(jUser.getId())
        .firstname(jUser.getFirstname())
        .lastname(jUser.getLastname())
        .email(jUser.getEmail())
        .role(jUser.getRole())
        .ref(jUser.getRef())
        .build();
  }
}
