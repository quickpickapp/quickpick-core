package com.quickpick.app.core.sms;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class SmsRateLimit {
  private final ConcurrentHashMap<String, Attempt> cache = new ConcurrentHashMap<>();

  private record Attempt(int count, long expiresAt) {}

  private static final int MAX_ATTEMPTS = 1;
  private static final long WINDOW_MS = 60_000;

  public boolean isAllowed(String phoneNumber) {
    long now = System.currentTimeMillis();

    var result = cache.compute(phoneNumber, (_, existing) -> {
      if (existing == null || now >= existing.expiresAt()) {
        return new Attempt(1, now + WINDOW_MS);
      }
      return new Attempt(existing.count() + 1, existing.expiresAt());
    });

    return result.count() <= MAX_ATTEMPTS;
  }

  @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
  public void evictExpired() {
    long now = System.currentTimeMillis();
    cache.entrySet().removeIf(e -> now >= e.getValue().expiresAt());
  }
}