package com.quickpick.app.core.api.app.authentication;

import com.quickpick.app.core.api.request.ApiRequestBody;
import com.quickpick.app.core.api.security.app.AppRestController;
import com.quickpick.app.core.email.EmailValidation;
import com.quickpick.app.core.mail.Mail;
import com.quickpick.app.core.user.User;
import com.quickpick.app.core.user.UserRepository;
import com.quickpick.app.core.user.verification.UserVerification;
import com.quickpick.app.core.user.verification.UserVerificationRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Key;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
public final class EmailAuthenticationController extends AppRestController {
  private final UserVerificationRepository userVerificationRepository;
  private final Mail verificationMail;
  private final VerificationCodeTemplate verificationCodeTemplate;
  private final AuthenticationSignup userBindSignup;
  private final AuthenticationCompletion userBindCompletion;

  private EmailAuthenticationController(
    @Qualifier("authenticationKey") Key authenticationKey,
    UserRepository userRepository,
    UserVerificationRepository userVerificationRepository,
    @Qualifier("verificationMail") Mail verificationMail,
    VerificationCodeTemplate verificationCodeTemplate,
    AuthenticationSignup userBindSignup,
    AuthenticationCompletion userBindCompletion
  ) {
    super(authenticationKey, userRepository);
    this.userVerificationRepository = userVerificationRepository;
    this.verificationMail = verificationMail;
    this.verificationCodeTemplate = verificationCodeTemplate;
    this.userBindSignup = userBindSignup;
    this.userBindCompletion = userBindCompletion;
  }

  @RequestMapping(path = "/authentication/request/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> requestUserBinding(
    @RequestBody String payload, HttpServletResponse response
  ) {
    var body = ApiRequestBody.of(payload, response);
    var email = body.getString("email");
    if (!EmailValidation.validate(email)) {
      return CompletableFuture.completedFuture(Map.of("success", false,
        "error", 1000));
    }
    return userRepository().existsByEmail(email)
      .thenCompose(exists -> requestUserBinding(email, exists));
  }

  private CompletableFuture<Map<String, Object>> requestUserBinding(
    String email, boolean userExists
  ) {
    if (!userExists) {
      return userBindSignup.generateAvailableUserId()
        .thenApply(userId -> requestUserBinding(userId, email));
    }
    return userRepository().findByEmail(email)
      .thenApply(user -> requestUserBinding(user.get().id(), email));
  }

  private static final String VERIFICATION_EMAIL_TITLE = "Verification";

  private Map<String, Object> requestUserBinding(
    UUID userId, String email
  ) {
    var verificationCode = VerificationCode.create(verificationCodeTemplate);
    verificationCode.generate();
    userVerificationRepository.save(UserVerification.create(userId, email,
      verificationCode.code()));
    verificationMail.send(email, VERIFICATION_EMAIL_TITLE,
      verificationCode.content());
    return Map.of("success", true, "user", userId);
  }

  @RequestMapping(path = "/authentication/complete/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> completeUserBinding(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = ApiRequestBody.of(payload, response);
    var userId = body.getUUID("user");
    return userVerificationRepository.existsById(userId)
      .thenCompose(exists -> completeUserBinding(request, body, userId,
        body.getString("code"), exists));
  }

  private CompletableFuture<Map<String, Object>> completeUserBinding(
    HttpServletRequest request, ApiRequestBody body, UUID userId, String code,
    boolean verificationExists
  ) {
    if (!verificationExists) {
      return CompletableFuture.completedFuture(Map.of("success", false,
        "error", 1000));
    }
    return userVerificationRepository.findById(userId)
      .thenCompose(verification -> userRepository().existsById(userId)
        .thenCompose(userExists -> completeUserBinding(request, body, userId,
          code, verification.get(), userExists)));
  }

  private CompletableFuture<Map<String, Object>> completeUserBinding(
    HttpServletRequest request, ApiRequestBody body, UUID userId, String code,
    UserVerification verification, boolean userExists
  ) {
    if (!verification.code().equals(code)) {
      return CompletableFuture.completedFuture(Map.of("success", false,
        "error", 1001));
    }
    if (!userExists) {
      return userBindSignup.signupUser(userId, verification.email(), body)
        .exceptionally(_ -> null)
        .thenCompose(user -> user == null ?
          CompletableFuture.completedFuture(Map.of("success", false, "error", 1002)) :
          completeUserBinding(request, user, verification));
    }
    return userRepository().findById(userId)
      .thenCompose(user -> completeUserBinding(request, user.get(), verification));
  }

  private CompletableFuture<Map<String, Object>> completeUserBinding(
    HttpServletRequest request, User user, UserVerification verification
  ) {
    userVerificationRepository.delete(verification);
    return userBindCompletion.completeBinding(request, user);
  }
}
