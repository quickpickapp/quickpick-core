package com.quickpick.app.core.api;

import com.quickpick.app.core.configuration.Configuration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.configuration2.INIConfiguration;

import java.util.List;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class ApiConfiguration implements Configuration {
  private int port;
  private String verificationKey;
  private String authenticationKey;
  private String refreshKey;
  private List<String> allowedOrigins;

  @Override
  public void load(INIConfiguration file) {
    port = file.getInt("api.port");
    verificationKey = file.getString("api.verification_key");
    authenticationKey = file.getString("api.authentication_key");
    refreshKey = file.getString("api.refresh_key");
    allowedOrigins = file.getList(String.class, "api.allowed_origins");
  }
}