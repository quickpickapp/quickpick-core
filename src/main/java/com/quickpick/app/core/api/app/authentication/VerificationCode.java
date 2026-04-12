package com.quickpick.app.core.api.app.authentication;

import com.quickpick.app.core.mail.MailTemplate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Random;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class VerificationCode {
  private final MailTemplate verificationCodeTemplate;
  private final Random secureRandom;
  private String code;
  private String content;

  public void generate() {
    code = generateCode();
    content = String.format(verificationCodeTemplate.mailTemplate(),
      code.substring(0, 3), code.substring(3));
  }

  private String generateCode() {
    var code = new StringBuilder(6);
    for (var i = 0; i < 6; i++) {
      var digit = secureRandom.nextInt(10);
      code.append(digit);
    }
    return code.toString();
  }
}
