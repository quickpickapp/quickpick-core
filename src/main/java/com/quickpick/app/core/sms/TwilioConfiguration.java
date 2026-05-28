package com.quickpick.app.core.sms;

import com.quickpick.app.core.configuration.Configuration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.configuration2.INIConfiguration;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class TwilioConfiguration implements Configuration {
  private String accountSid;
  private String authToken;
  private String phoneNumber;
  private String verifyServiceSid;

  @Override
  public void load(INIConfiguration file) {
    accountSid = file.getString("twilio.account_sid");
    authToken = file.getString("twilio.auth_token");
    phoneNumber = file.getString("twilio.phone_number");
    verifyServiceSid = file.getString("twilio.verify_service_sid");
  }
}
