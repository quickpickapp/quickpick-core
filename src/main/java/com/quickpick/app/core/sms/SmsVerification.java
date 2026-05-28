package com.quickpick.app.core.sms;

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
    return Verification.creator(configuration.verifyServiceSid(), phoneNumber, "sms")
      .createAsync();
  }

  public CompletableFuture<Boolean> verifyCode(String phoneNumber, String code) {
    return VerificationCheck.creator(configuration.verifyServiceSid())
      .setTo(phoneNumber).setCode(code).createAsync()
      .thenApply(check -> "approved".equals(check.getStatus().toString()));
  }
}
