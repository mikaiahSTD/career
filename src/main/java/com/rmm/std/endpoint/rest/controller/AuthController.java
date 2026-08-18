package com.rmm.std.endpoint.rest.controller;

import com.rmm.std.dto.LoginRequest;
import com.rmm.std.dto.LoginResponse;
import com.rmm.std.dto.UserRequest;
import com.rmm.std.dto.UserResponse;
import com.rmm.std.security.JwtService;
import com.rmm.std.security.TokenBlacklistService;
import com.rmm.std.security.TokenResolver;
import com.rmm.std.security.UserPrincipal;
import com.rmm.std.service.UserService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {

  private static final String JWT_COOKIE = "jwt_token";

  private AuthenticationManager authenticationManager;
  private JwtService jwtService;
  private UserService userService;
  private final UserDetailsService userDetailsService;
  private final TokenResolver tokenResolver;
  private final TokenBlacklistService tokenBlacklistService;

  @PostMapping("/register")
  public ResponseEntity<?> registerUser(@RequestBody @Valid UserRequest req) {
    return ResponseEntity.ok().body(userService.createUser(req));
  }

  @PostMapping("/login")
  public ResponseEntity<?> createAuthenticationToken(
      @RequestBody @Valid LoginRequest req, HttpServletRequest request) throws Exception {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(req.email(), req.password()));

    final UserDetails userDetails = userDetailsService.loadUserByUsername(req.email());

    final String jwt = jwtService.generateToken(userDetails);

    var user = userService.findByEmail(req.email());

    ResponseCookie cookie =
        ResponseCookie.from(JWT_COOKIE, jwt)
            .httpOnly(true)
            .secure(request.isSecure())
            .path("/")
            .sameSite("Lax")
            .maxAge(Duration.ofMillis(jwtService.getExpirationMs()))
            .build();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(LoginResponse.of(jwt, user.getId(), user.getRole().name()));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request) {
    String token = tokenResolver.resolve(request);
    if (token != null) {
      try {
        tokenBlacklistService.revoke(token, jwtService.extractExpiration(token).getTime());
      } catch (JwtException | IllegalArgumentException e) {
        // ignore: malformed or already-expired tokens cannot be revoked
      }
    }

    ResponseCookie cookie =
        ResponseCookie.from(JWT_COOKIE, "")
            .httpOnly(true)
            .secure(request.isSecure())
            .path("/")
            .sameSite("Lax")
            .maxAge(Duration.ZERO)
            .build();

    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
  }

  @GetMapping("/me")
  public UserResponse me(@AuthenticationPrincipal UserPrincipal principal) {
    return userService.get(principal.getUser().getId());
  }
}
