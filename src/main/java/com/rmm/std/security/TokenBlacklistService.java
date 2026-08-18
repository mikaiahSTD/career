package com.rmm.std.security;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

  private static final int MAX_ENTRIES = 1000;

  private final ConcurrentHashMap<String, Long> revoked = new ConcurrentHashMap<>();

  public void revoke(String token, long expiresAtEpochMillis) {
    if (token == null) {
      return;
    }
    if (revoked.size() >= MAX_ENTRIES) {
      purgeExpired();
    }
    revoked.put(token, expiresAtEpochMillis);
  }

  public boolean isRevoked(String token) {
    if (token == null) {
      return false;
    }
    Long expiresAt = revoked.get(token);
    if (expiresAt == null) {
      return false;
    }
    if (expiresAt <= System.currentTimeMillis()) {
      revoked.remove(token);
      return false;
    }
    return true;
  }

  private void purgeExpired() {
    long now = System.currentTimeMillis();
    revoked.entrySet().removeIf(entry -> entry.getValue() <= now);
  }
}
