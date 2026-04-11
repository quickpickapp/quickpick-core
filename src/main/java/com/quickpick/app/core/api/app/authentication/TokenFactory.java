package com.quickpick.app.core.api.app.authentication;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public final class TokenFactory {
  private final Key authenticationKey;
  private final Key refreshKey;

  public TokenFactory(
    @Qualifier("authenticationKey") Key authenticationKey,
    @Qualifier("refreshKey") Key refreshKey
  ) {
    this.authenticationKey = authenticationKey;
    this.refreshKey = refreshKey;
  }

  private static final long MAXIMUM_AUTHENTICATION_EXPIRATION_TIME =
    1000L * 60 * 10;

  public String generateAuthenticationToken(UUID userId, UUID sessionId) {
    var expirationDate = new Date(System.currentTimeMillis() +
      MAXIMUM_AUTHENTICATION_EXPIRATION_TIME);
    return Jwts.builder().expiration(expirationDate)
      .claim("id", userId.toString())
      .claim("session", sessionId.toString())
      .signWith(authenticationKey)
      .compact();
  }

  private static final long MAXIMUM_REFRESH_EXPIRATION_TIME =
    1000L * 60 * 60 * 24 * 365;

  public String generateRefreshToken(UUID userId, UUID sessionId) {
    var expirationDate = new Date(System.currentTimeMillis() +
      MAXIMUM_REFRESH_EXPIRATION_TIME);
    return Jwts.builder().expiration(expirationDate)
      .claim("id", userId.toString())
      .claim("session", sessionId.toString())
      .signWith(refreshKey)
      .compact();
  }
}
