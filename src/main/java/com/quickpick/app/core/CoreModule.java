package com.quickpick.app.core;

import com.quickpick.app.core.event.EventExecutor;
import com.quickpick.app.core.event.HookRegistry;
import com.quickpick.app.core.log.Log;
import com.quickpick.app.core.api.ApiModule;
import org.apache.commons.configuration2.AbstractConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.io.File;

@Configuration
@Import(ApiModule.class)
public class CoreModule {
  @Bean
  Log coreLog() throws Exception {
    return Log.create("Core");
  }

  @Bean
  AbstractConfiguration configurationFile() throws Exception {
    return new Configurations().ini(new File("config.ini"));
  }

  @Bean
  HookRegistry hookRegistry() {
    return HookRegistry.create();
  }

  @Bean
  EventExecutor eventExecutor(HookRegistry registry, Log log) {
    return EventExecutor.create(registry, log);
  }
}
