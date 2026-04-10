package com.quickpick.app.core.api.app.authentication;

import com.quickpick.app.core.api.request.ApiRequestBody;
import com.quickpick.app.core.api.security.app.AppRestController;
import com.quickpick.app.core.user.User;
import com.quickpick.app.core.user.UserRepository;
import com.quickpick.app.core.user.session.UserSession;
import com.quickpick.app.core.user.session.UserSessionRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Key;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
public final class SessionAuthenticationController extends AppRestController {
  private final Key refreshKey;
  private final TokenFactory tokenFactory;
  private final UserSessionRepository sessionRepository;

  private SessionAuthenticationController(
    @Qualifier("authenticationKey") Key authenticationKey,
    @Qualifier("refreshKey") Key refreshKey,
    UserRepository userRepository, TokenFactory tokenFactory,
    UserSessionRepository sessionRepository
  ) {
    super(authenticationKey, userRepository);
    this.refreshKey = refreshKey;
    this.tokenFactory = tokenFactory;
    this.sessionRepository = sessionRepository;
  }

  @RequestMapping(path = "/authentication/refresh/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> refreshAuthorization(
    @RequestBody String payload, HttpServletResponse response
  ) {
    var body = ApiRequestBody.of(payload, response);
    var refreshToken = body.getString("refresh_token");
    var result = verifyToken(refreshKey, refreshToken);
    if (result == null) {
      return CompletableFuture.completedFuture(Map.of("success", false));
    }
    var userId = UUID.fromString(result.get("id", String.class));
    var sessionId = UUID.fromString(result.get("session", String.class));
    return userRepository().existsById(userId)
      .thenCompose(userExists -> sessionRepository.existsById(sessionId)
        .thenCompose(sessionExists -> refreshAuthorization(refreshToken, userId,
          sessionId, userExists, sessionExists)));
  }

  private CompletableFuture<Map<String, Object>> refreshAuthorization(
    String refreshToken, UUID userId, UUID sessionId, boolean userExists,
    boolean sessionExists
  ) {
    if (!userExists || !sessionExists) {
      return CompletableFuture.completedFuture(Map.of("success", false));
    }
    return userRepository().findById(userId)
      .thenCompose(user -> sessionRepository.findById(sessionId)
        .thenApply(session -> refreshAuthorization(refreshToken,
          user.get(), session.get())));
  }

  private Map<String, Object> refreshAuthorization(
    String refreshToken, User user, UserSession session
  ) {
    if (session.status().isClosed() ||
      !session.lastRefreshToken().equals(refreshToken)
    ) {
      return Map.of("success", false);
    }
    var newAuthenticationToken = tokenFactory.generateAuthenticationToken(
      user.id(), session.id());
    var newRefreshToken = tokenFactory.generateRefreshToken(user.id(),
      session.id());
    session.updateRefreshToken(newRefreshToken);
    sessionRepository.save(session);
    return Map.of("success", true, "authentication_token", newAuthenticationToken,
      "refresh_token", newRefreshToken);
  }

  @RequestMapping(path = "/logout/", method = RequestMethod.GET)
  public CompletableFuture<Void> logout(
    HttpServletRequest request, HttpServletResponse response
  ) {
    var sessionId = findSessionId(request);
    return findUser(request, response)
      .thenCompose(user -> user == null ?
        CompletableFuture.completedFuture(null) :
        sessionRepository.findById(sessionId).thenApply(Optional::get)
          .thenCompose(this::closeSession));
  }

  private CompletableFuture<Void> closeSession(UserSession session) {
    session.close();
    return sessionRepository.save(session).thenApply(_ -> null);
  }

  @RequestMapping(path = "/authorized/", method = RequestMethod.GET)
  public CompletableFuture<Map<String, Object>> isAuthorized(
    HttpServletRequest request, HttpServletResponse response
  ) {
    return findUser(request, response)
      .thenApply(user -> Map.of("authorized", user != null));
  }

  private Claims verifyToken(Key key, String token) {
    try {
      return Jwts.parser()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getPayload();
    } catch (Exception exception) {
      return null;
    }
  }
}