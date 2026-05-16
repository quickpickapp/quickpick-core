package com.quickpick.app.core.mail;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailModule {
  @Bean
  @Qualifier("mailTemplate")
  MailTemplate mailTemplate() throws Exception {
    return MailTemplate.createAndLoad();
  }

  @Bean
  @Qualifier("verificationCodeTemplate")
  MailTemplate verificationCodeTemplate() throws Exception {
    return MailTemplate.createAndLoad("verification-code-template");
  }
}
