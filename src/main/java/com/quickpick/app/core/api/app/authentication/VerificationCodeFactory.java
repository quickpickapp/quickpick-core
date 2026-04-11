package com.quickpick.app.core.api.app.authentication;

import com.quickpick.app.core.mail.MailTemplate;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;

@Configuration
public final class VerificationCodeFactory {
  private final MailTemplate verificationCodeTemplate;
  private final SecureRandom secureRandom = new SecureRandom();

  private VerificationCodeFactory(
    @Qualifier("verificationCodeTemplate") MailTemplate verificationCodeTemplate
  ) {
    this.verificationCodeTemplate = verificationCodeTemplate;
  }

  public VerificationCode generate() {
    var code = VerificationCode.create(verificationCodeTemplate);
    code.generate();
    return code;
  }
}
