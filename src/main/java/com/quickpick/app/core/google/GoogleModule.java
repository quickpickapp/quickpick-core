package com.quickpick.app.core.google;

import org.apache.commons.configuration2.AbstractConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleModule {
  @Bean
  public GoogleConfiguration googleConfiguration(AbstractConfiguration file) {
    var configuration = GoogleConfiguration.create();
    configuration.load(file);
    return configuration;
  }
}