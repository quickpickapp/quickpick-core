package de.lukasbreuer.quickpick.core;

import de.lukasbreuer.quickpick.core.event.EventExecutor;
import de.lukasbreuer.quickpick.core.event.HookRegistry;
import de.lukasbreuer.quickpick.core.log.Log;
import de.lukasbreuer.quickpick.core.api.ApiConfiguration;
import de.lukasbreuer.quickpick.core.application.ApplicationLaunchEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Collections;

@SpringBootApplication(scanBasePackages = {"de.lukasbreuer.quickpick.core"})
public class CoreApplication {
  /**
   * The starting point where the application is executed
   *
   * @param args The arguments that are passed into the application
   */
  public static void main(String[] args) {
    try (var coreContext = new AnnotationConfigApplicationContext(CoreModule.class)) {
      var log = coreContext.getBean(Log.class);
      Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
        log.processError(throwable));
      try {
        log.info("Initializing QuickPick - Core");
        var application = new SpringApplication(CoreApplication.class);
        var apiConfiguration = coreContext.getBean(ApiConfiguration.class);
        application.setDefaultProperties(Collections.singletonMap("server.port",
          apiConfiguration.port()));
        log.info("Booting Spring...");
        var applicationContext = application.run(args);
        var eventExecutor = applicationContext.getBean(EventExecutor.class);
        registerHooks(applicationContext);
        log.info("Spring successfully booted");
        log.info("Successfully booted QuickPick - Core");
        eventExecutor.execute(ApplicationLaunchEvent.create());
      } catch (Exception exception) {
        log.processError(exception);
      }
    }
  }

  private static void registerHooks(ConfigurableApplicationContext context) {
    var hookRegistry = context.getBean(HookRegistry.class);
  }
}