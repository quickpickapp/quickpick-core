package com.quickpick.app.core.notification;

import com.quickpick.app.core.configuration.Configuration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.configuration2.AbstractConfiguration;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class FirebaseConfiguration implements Configuration {
  private String configurationName;
  private String projectId;

  @Override
  public void load(AbstractConfiguration file) {
    configurationName = file.getString("firebase.configuration");
    projectId = file.getString("firebase.project_id");
  }
}
