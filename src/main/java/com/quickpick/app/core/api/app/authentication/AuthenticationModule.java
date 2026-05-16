package com.quickpick.app.core.api.app.authentication;

import com.quickpick.app.core.mail.Mail;
import com.quickpick.app.core.mail.MailFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthenticationModule {
  private @Autowired MailFactory mailFactory;

  @Bean
  @Qualifier("verificationMail")
  Mail vericationMail() {
    return mailFactory.create("verification");
  }
}
