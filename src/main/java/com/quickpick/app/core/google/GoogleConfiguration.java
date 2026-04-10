package com.quickpick.app.core.google;

import com.quickpick.app.core.configuration.Configuration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.configuration2.AbstractConfiguration;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class GoogleConfiguration implements Configuration {
  private String clientId;
  private String clientSecret;

  @Override
  public void load(AbstractConfiguration file) {
    clientId = file.getString("google.client.id");
    clientSecret = file.getString("google.secret.id");
  }
}