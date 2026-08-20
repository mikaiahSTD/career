package com.rmm.std.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.stereotype.Component;

@Component
public class JwtCookieCsrfTokenRepository implements CsrfTokenRepository {

  public static final String JWT_COOKIE = "jwt_token";
  public static final String HEADER_NAME = "X-XSRF-TOKEN";
  public static final String PARAMETER_NAME = "_csrf";

  private final byte[] secretBytes;

  public JwtCookieCsrfTokenRepository(@Value("${app.jwt.secret}") String base64Secret) {
    this.secretBytes = Base64.getDecoder().decode(base64Secret);
  }

  @Override
  public CsrfToken generateToken(HttpServletRequest request) {
    return new DefaultCsrfToken(HEADER_NAME, PARAMETER_NAME, tokenFor(request));
  }

  @Override
  public void saveToken(
      CsrfToken token, HttpServletRequest request, HttpServletResponse response) {}

  @Override
  public CsrfToken loadToken(HttpServletRequest request) {
    String jwt = cookieValue(request, JWT_COOKIE);
    if (jwt == null) {
      return null;
    }
    return new DefaultCsrfToken(HEADER_NAME, PARAMETER_NAME, hmac(jwt));
  }

  private String tokenFor(HttpServletRequest request) {
    String jwt = cookieValue(request, JWT_COOKIE);
    return jwt != null ? hmac(jwt) : UUID.randomUUID().toString();
  }

  private String cookieValue(HttpServletRequest request, String name) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    for (Cookie cookie : cookies) {
      if (name.equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }

  private String hmac(String value) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secretBytes, "HmacSHA256"));
      return Base64.getUrlEncoder()
          .withoutPadding()
          .encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("HMAC-SHA256 is unavailable", e);
    }
  }
}
