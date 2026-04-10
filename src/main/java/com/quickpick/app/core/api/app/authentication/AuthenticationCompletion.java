package com.quickpick.app.core.api.app.authentication;

import com.maxmind.geoip2.DatabaseReader;
import com.quickpick.app.core.user.User;
import com.quickpick.app.core.user.session.UserAgent;
import com.quickpick.app.core.user.session.UserSession;
import com.quickpick.app.core.user.session.UserSessionRepository;
import com.quickpick.app.core.user.session.UserSessionStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Configuration
@RequiredArgsConstructor
public class AuthenticationCompletion {
  private final TokenFactory tokenFactory;
  private final UserSessionRepository sessionRepository;
  private final DatabaseReader geoDatabaseReader;

  public CompletableFuture<Map<String, Object>> completeBinding(
    HttpServletRequest request, User user
  ) {
    return sessionRepository.generateAvailableId(UUID::randomUUID)
      .thenComposeAsync(sessionId -> completeBinding(request, user, sessionId));
  }

  private CompletableFuture<Map<String, Object>> completeBinding(
    HttpServletRequest request, User user, UUID sessionId
  ) {
    var authenticationToken = tokenFactory.generateAuthenticationToken(
      user.id(), sessionId);
    var refreshToken = tokenFactory.generateRefreshToken(user.id(), sessionId);
    return storeSession(request, user.id(), sessionId, refreshToken)
      .thenApply(_ -> Map.of("success", true, "email", user.email(),
        "user", user.id(), "authentication_token", authenticationToken,
        "refresh_token", refreshToken));
  }

  private CompletableFuture<UserSession> storeSession(
    HttpServletRequest request, UUID userId, UUID sessionId, String refreshToken
  ) {
    var country = "";
    var city = "";
    var platform = "";
    var ipAddress = request.getHeader("X-Real-IP");
    try {
      var location = geoDatabaseReader.city(InetAddress.getByName(ipAddress));
      country = location.getCountry().getName();
      city = location.getCity().getName();
      platform = UserAgent.create(request.getHeader("User-Agent")).findPlatform();
    } catch (Exception ignored) {
    }
    var session = UserSession.create(sessionId, userId,
      UserSessionStatus.ACTIVE, platform, ipAddress, country, city,
      System.currentTimeMillis(), refreshToken, System.currentTimeMillis());
    return sessionRepository.save(session);
  }
}
