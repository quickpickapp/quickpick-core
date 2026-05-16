package com.quickpick.app.core.google;

import org.apache.commons.configuration2.INIConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleModule {
  @Bean
  public GoogleConfiguration googleConfiguration(INIConfiguration file) {
    var configuration = GoogleConfiguration.create();
    configuration.load(file);
    return configuration;
  }
}