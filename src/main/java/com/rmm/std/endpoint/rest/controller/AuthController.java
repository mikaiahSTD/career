package com.rmm.std.endpoint.rest.controller;

import com.rmm.std.dto.LoginRequest;
import com.rmm.std.dto.LoginResponse;
import com.rmm.std.dto.UserRequest;
import com.rmm.std.security.JwtService;
import com.rmm.std.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {
  private AuthenticationManager authenticationManager;
  private JwtService jwtService;
  private UserService userService;
  private final UserDetailsService userDetailsService;

  @PostMapping("/register")
  public ResponseEntity<?> registerUser(@RequestBody @Valid UserRequest req) {
    return ResponseEntity.ok().body(userService.createUser(req));
  }

  @PostMapping("/login")
  public ResponseEntity<?> createAuthenticationToken(@RequestBody @Valid LoginRequest req)
      throws Exception {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(req.email(), req.password()));

    final UserDetails userDetails = userDetailsService.loadUserByUsername(req.email());

    final String jwt = jwtService.generateToken(userDetails);

    return ResponseEntity.ok().body(LoginResponse.of(jwt));
  }
}
