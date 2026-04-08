package com.quickpick.app.core.statistic;

import com.quickpick.app.core.configuration.Configuration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.configuration2.AbstractConfiguration;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class StatisticConfiguration implements Configuration {
  private String statisticKey;

  @Override
  public void load(AbstractConfiguration file) {
    statisticKey = file.getString("statistic.key");
  }
}