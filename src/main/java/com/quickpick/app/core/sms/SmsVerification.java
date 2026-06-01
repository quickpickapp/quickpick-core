package com.quickpick.app.core.sms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CompletableFuture;

@Configuration
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class SmsVerification {
  private final TwilioConfiguration configuration;

  public CompletableFuture<Verification> sendVerificationCode(String phoneNumber) {
    if (configuration.tests().containsKey(phoneNumber)) {
      return CompletableFuture.completedFuture(mockPendingVerification(phoneNumber));
    }
    return Verification.creator(configuration.verifyServiceSid(), phoneNumber, "sms")
      .createAsync();
  }

  private Verification mockPendingVerification(String phoneNumber) {
    return Verification.fromJson(
      """
      {"status": "pending", "to": "%s", "valid": false}
      """.formatted(phoneNumber),
      new ObjectMapper()
    );
  }

  public CompletableFuture<Boolean> verifyCode(String phoneNumber, String code) {
    if (configuration.tests().containsKey(phoneNumber) &&
      configuration.tests().get(phoneNumber).equals(code)
    ) {
      return CompletableFuture.completedFuture(true);
    }
    return VerificationCheck.creator(configuration.verifyServiceSid())
      .setTo(phoneNumber).setCode(code).createAsync()
      .thenApply(check -> "approved".equals(check.getStatus().toString()));
  }
}
