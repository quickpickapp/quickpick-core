package com.quickpick.app.core.sms;

import com.twilio.Twilio;
import org.apache.commons.configuration2.INIConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SmsModule {
  @Bean
  public TwilioConfiguration twilioConfiguration(INIConfiguration file) {
    var configuration = TwilioConfiguration.create();
    configuration.load(file);
    Twilio.init(configuration.accountSid(), configuration.authToken());
    return configuration;
  }
}