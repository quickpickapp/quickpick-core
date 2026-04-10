package com.quickpick.app.core.api.app.authentication;

import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.quickpick.app.core.api.request.ApiRequestBody;
import com.quickpick.app.core.api.security.app.AppRestController;
import com.quickpick.app.core.apple.AppleAuthentication;
import com.quickpick.app.core.apple.AppleAuthenticationRepository;
import com.quickpick.app.core.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;
import java.security.Key;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
public final class AppleAuthenticationController extends AppRestController {
  private final AuthenticationSignup userBindSignup;
  private final AuthenticationCompletion userBindCompletion;
  private final AppleAuthenticationRepository appleAuthenticationRepository;

  private AppleAuthenticationController(
    @Qualifier("authenticationKey") Key authenticationKey,
    UserRepository userRepository,
    AuthenticationSignup userBindSignup,
    AuthenticationCompletion userBindCompletion,
    AppleAuthenticationRepository appleAuthenticationRepository
  ) {
    super(authenticationKey, userRepository);
    this.userBindSignup = userBindSignup;
    this.userBindCompletion = userBindCompletion;
    this.appleAuthenticationRepository = appleAuthenticationRepository;
  }

  @RequestMapping(path = "/authentication/apple/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> bindAppleUser(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = ApiRequestBody.of(payload, response);
    var futureResponse = new CompletableFuture<Map<String, Object>>();
    new Thread(() -> bindAppleUser(request, body, body.getString("token"))
      .thenAccept(futureResponse::complete)).start();
    return futureResponse;
  }

  public CompletableFuture<Map<String, Object>> bindAppleUser(
    HttpServletRequest request, ApiRequestBody body, String token
  ) {
    try {
      var jwt = SignedJWT.parse(token);
      if (!verifyAppleJWT(jwt)) {
        return CompletableFuture.completedFuture(Map.of("success", false,
          "error", 1000));
      }
      var claims = jwt.getJWTClaimsSet();
      if (!checkAppleClaimFormalities(claims)) {
        return CompletableFuture.completedFuture(Map.of("success", false,
          "error", 1001));
      }
      var subject = claims.getSubject();
      if (subject == null) {
        return CompletableFuture.completedFuture(Map.of("success", false,
          "error", 1002));
      }
      var email = claims.getStringClaim("email");
      var emailVerified = claims.getBooleanClaim("email_verified");
      if (email != null && emailVerified != null && emailVerified) {
        appleAuthenticationRepository.save(
          AppleAuthentication.create(subject, email));
        return userRepository().existsByEmail(email)
          .thenCompose(exists -> bindAppleUser(request, body, email, exists));
      }
      return appleAuthenticationRepository.existsById(subject)
        .thenCompose(exists -> bindKnownAppleUser(request, body, subject, exists));
    } catch (Exception exception) {
      return CompletableFuture.completedFuture(Map.of("success", false,
        "error", 1003));
    }
  }

  private CompletableFuture<Map<String, Object>> bindKnownAppleUser(
    HttpServletRequest request, ApiRequestBody body, String subject,
    boolean subjectExists
  ) {
    if (!subjectExists) {
      return CompletableFuture.completedFuture(Map.of("success", false,
        "error", 1004));
    }
    return appleAuthenticationRepository.findById(subject)
      .thenApply(Optional::get)
      .thenApply(AppleAuthentication::email)
      .thenCompose(email -> userRepository().existsByEmail(email)
        .thenCompose(exists -> bindAppleUser(request, body, email, exists)));
  }

  private boolean verifyAppleJWT(SignedJWT jwt) throws Exception {
    var publicKeys = JWKSet.load(new URL("https://appleid.apple.com/auth/keys"));
    var key = publicKeys.getKeyByKeyId(jwt.getHeader().getKeyID());
    return key != null && jwt.verify(new RSASSAVerifier((RSAKey) key));
  }

  private boolean checkAppleClaimFormalities(JWTClaimsSet claims) {
    var audience = claims.getAudience().get(0);
    var issuer = claims.getIssuer();
    return "https://appleid.apple.com".equals(issuer) &&
      "com.quickpick.app".equals(audience);
  }

  private CompletableFuture<Map<String, Object>> bindAppleUser(
    HttpServletRequest request, ApiRequestBody body, String email,
    boolean userExists
  ) {
    if (!userExists) {
      return userBindSignup.signupUser(email, body)
        .exceptionally(_ -> null)
        .thenCompose(newUser -> newUser == null ?
          CompletableFuture.completedFuture(Map.of("success", false, "error", 1005)) :
          userBindCompletion.completeBinding(request, newUser));
    }
    return userRepository().findByEmail(email)
      .thenCompose(user -> userBindCompletion.completeBinding(request, user.get()));
  }
}