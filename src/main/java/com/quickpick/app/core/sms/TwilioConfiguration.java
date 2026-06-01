package com.quickpick.app.core.sms;

import com.google.common.collect.Maps;
import com.quickpick.app.core.configuration.Configuration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.configuration2.INIConfiguration;

import java.util.Map;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class TwilioConfiguration implements Configuration {
  private String accountSid;
  private String authToken;
  private String phoneNumber;
  private String verifyServiceSid;
  private Map<String, String> tests;

  @Override
  public void load(INIConfiguration file) {
    accountSid = file.getString("twilio.account_sid");
    authToken = file.getString("twilio.auth_token");
    phoneNumber = file.getString("twilio.phone_number");
    verifyServiceSid = file.getString("twilio.verify_service_sid");
    tests = Maps.newHashMap();
    file.getSections().forEach(section -> {
      if (section != null && section.startsWith("twilio.test")) {
        var sub = file.getSection(section);
        var phone = sub.getString("phone_number");
        var code = sub.getString("verification_code");
        if (phone != null && code != null) {
          tests.put(phone, code);
        }
      }
    });
  }
}
