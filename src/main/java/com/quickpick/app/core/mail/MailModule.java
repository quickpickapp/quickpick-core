package com.quickpick.app.core.mail;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailModule {
  private @Autowired MailFactory mailFactory;
  private Mail verificationMail;
  private MailTemplate mailTemplate;
  private MailTemplate verificationCodeTemplate;

  @Bean
  @Qualifier("verificationMail")
  Mail vericationMail() {
    return verificationMail;
  }

  @Bean
  @Qualifier("mailTemplate")
  MailTemplate mailTemplate() {
    return mailTemplate;
  }

  @Bean
  @Qualifier("verificationCodeTemplate")
  MailTemplate verificationCodeTemplate() {
    return verificationCodeTemplate;
  }

  @PostConstruct
  private void initializeVerificationMail() throws Exception {
    verificationMail = mailFactory.create("verification");
  }

  @PostConstruct
  private void initializeMailTemplate() throws Exception {
    mailTemplate = MailTemplate.createAndLoad();
  }

  @PostConstruct
  private void initializeVerificationCodeTemplate() throws Exception {
    verificationCodeTemplate = MailTemplate.createAndLoad("verification-code-template");
  }
}
