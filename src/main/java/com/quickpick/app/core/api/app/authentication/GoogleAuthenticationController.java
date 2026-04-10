package com.quickpick.app.core.api.app.authentication;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.quickpick.app.core.api.request.ApiRequestBody;
import com.quickpick.app.core.api.security.app.AppRestController;
import com.quickpick.app.core.google.GoogleConfiguration;
import com.quickpick.app.core.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Key;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
public final class GoogleAuthenticationController extends AppRestController {
  private final AuthenticationSignup userBindSignup;
  private final AuthenticationCompletion userBindCompletion;
  private final GoogleConfiguration googleConfiguration;
  private final GsonFactory gsonFactory = GsonFactory.getDefaultInstance();
  private final NetHttpTransport netHttpTransport = new NetHttpTransport();

  private GoogleAuthenticationController(
    @Qualifier("authenticationKey") Key authenticationKey,
    UserRepository userRepository, AuthenticationSignup userBindSignup,
    AuthenticationCompletion userBindCompletion,
    GoogleConfiguration googleConfiguration
  ) {
    super(authenticationKey, userRepository);
    this.userBindSignup = userBindSignup;
    this.userBindCompletion = userBindCompletion;
    this.googleConfiguration = googleConfiguration;
  }

  @RequestMapping(path = "/authentication/google/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> bindGoogleUser(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = ApiRequestBody.of(payload, response);
    return bindGoogleUser(request, body, body.getString("token"));
  }

  public CompletableFuture<Map<String, Object>> bindGoogleUser(
    HttpServletRequest request, ApiRequestBody body, String token
  ) {
    try {
      var verifier = new GoogleIdTokenVerifier.Builder(netHttpTransport, gsonFactory)
        .setAudience(Collections.singletonList(googleConfiguration.clientId()))
        .build();
      var idToken = verifier.verify(token);
      if (idToken == null) {
        return CompletableFuture.completedFuture(Map.of("success", false,
          "error", 1000));
      }
      var payload = idToken.getPayload();
      var email = payload.getEmail();
      return userRepository().existsByEmail(email)
        .thenCompose(userExists -> bindGoogleUser(request, body, email, userExists));
    } catch (Exception exception) {
      return CompletableFuture.completedFuture(Map.of("success", false,
        "error", 1001));
    }
  }

  private CompletableFuture<Map<String, Object>> bindGoogleUser(
    HttpServletRequest request, ApiRequestBody body, String email,
    boolean userExists
  ) {
    if (!userExists) {
      return userBindSignup.signupUser(email, body)
        .exceptionally(_ -> null)
        .thenCompose(newUser -> newUser == null ?
          CompletableFuture.completedFuture(Map.of("success", false, "error", 1002)) :
          userBindCompletion.completeBinding(request, newUser));
    }
    return userRepository().findByEmail(email)
      .thenCompose(user -> userBindCompletion.completeBinding(request, user.get()));
  }
}