package com.quickpick.app.core.api.app.authentication;

import com.google.common.collect.Maps;
import com.quickpick.app.core.api.request.ApiRequestBody;
import com.quickpick.app.core.api.security.app.AppRestController;
import com.quickpick.app.core.email.EmailValidation;
import com.quickpick.app.core.mail.Mail;
import com.quickpick.app.core.user.User;
import com.quickpick.app.core.user.UserRepository;
import com.quickpick.app.core.user.change.UserEmailChange;
import com.quickpick.app.core.user.change.UserEmailChangeRepository;
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
import java.util.concurrent.CompletableFuture;

@RestController
public final class EmailChangeController extends AppRestController {
  private final UserEmailChangeRepository emailChangeRepository;
  private final Mail verificationMail;
  private final VerificationCodeTemplate verificationCodeTemplate;

  private EmailChangeController(
    @Qualifier("authenticationKey") Key authenticationKey,
    UserRepository userRepository,
    UserEmailChangeRepository emailChangeRepository,
    @Qualifier("verificationMail") Mail verificationMail,
    VerificationCodeTemplate verificationCodeTemplate
  ) {
    super(authenticationKey, userRepository);
    this.emailChangeRepository = emailChangeRepository;
    this.verificationMail = verificationMail;
    this.verificationCodeTemplate = verificationCodeTemplate;
  }

  @RequestMapping(path = "/email/change/request/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> requestUserEmailChange(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = ApiRequestBody.of(payload, response);
    var email = body.getString("email");
    if (!EmailValidation.validate(email)) {
      return CompletableFuture.completedFuture(Map.of("success", false,
        "error", 1000));
    }
    return findUser(request, response)
      .thenCompose(user -> userRepository().existsByEmail(email)
        .thenApply(exists -> requestUserEmailChange(user, email, exists)));
  }

  private static final String VERIFICATION_EMAIL_TITLE = "Verification";

  private Map<String, Object> requestUserEmailChange(
    User user, String email, boolean emailExists
  ) {
    if (user == null) {
      return Maps.newHashMap();
    }
    if (emailExists) {
      return Map.of("success", false, "error", 1001);
    }
    var verification = VerificationCode.create(verificationCodeTemplate);
    verification.generate();
    emailChangeRepository.save(UserEmailChange.create(
      user.id(), email, verification.code()));
    verificationMail.send(email, VERIFICATION_EMAIL_TITLE, verification.content());
    return Map.of("success", true);
  }

  @RequestMapping(path = "/email/change/complete/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> completeUserEmailChange(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = ApiRequestBody.of(payload, response);
    var code = body.getString("code");
    return findUser(request, response)
      .thenCompose(user -> completeUserEmailChange(user, code));
  }

  private CompletableFuture<Map<String, Object>> completeUserEmailChange(
    User user, String code
  ) {
    if (user == null) {
      return CompletableFuture.completedFuture(Maps.newHashMap());
    }
    return emailChangeRepository.existsById(user.id())
      .thenCompose(exists -> completeUserEmailChange(user, code, exists));
  }

  private CompletableFuture<Map<String, Object>> completeUserEmailChange(
    User user, String code, boolean emailChangeExists
  ) {
    if (!emailChangeExists) {
      return CompletableFuture.completedFuture(Map.of("success", false,
        "error", 1000));
    }
    return emailChangeRepository.findById(user.id())
      .thenApply(Optional::get)
      .thenCompose(change -> userRepository().existsByEmail(change.newEmail())
        .thenApply(exists -> completeUserEmailChange(user, code, change, exists)));
  }

  private Map<String, Object> completeUserEmailChange(
    User user, String code, UserEmailChange emailChange, boolean emailExists
  ) {
    if (!emailChange.code().equals(code)) {
      return Map.of("success", false, "error", 1001);
    }
    if (emailExists) {
      return Map.of("success", false, "error", 1002);
    }
    emailChangeRepository.delete(emailChange);
    user.changeEmail(emailChange.newEmail());
    userRepository().save(user);
    return Map.of("success", true, "email", emailChange.newEmail());
  }
}
