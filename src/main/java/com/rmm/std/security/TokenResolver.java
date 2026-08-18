package com.rmm.std.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class TokenResolver {

  public static final String HEADER = "Authorization";
  public static final String PREFIX = "Bearer ";
  public static final String COOKIE_NAME = "jwt_token";

  public String resolve(HttpServletRequest request) {
    String authHeader = request.getHeader(HEADER);
    if (authHeader != null && authHeader.startsWith(PREFIX)) {
      return authHeader.substring(PREFIX.length());
    }
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (COOKIE_NAME.equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }
}
