package com.quickpick.app.core.notification;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.apache.commons.configuration2.AbstractConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;

@Configuration
public class NotificationModule {
  @Bean
  public FirebaseConfiguration firebaseConfiguration(AbstractConfiguration file) {
    var configuration = FirebaseConfiguration.create();
    configuration.load(file);
    return configuration;
  }

  @Bean
  GoogleCredentials googleCredentials(
    FirebaseConfiguration configuration
  ) throws Exception {
    return ServiceAccountCredentials
      .fromStream(new FileInputStream(System.getProperty("user.dir") +
        "/configurations/notification/" + configuration.configurationName()))
      .createScoped("https://www.googleapis.com/auth/firebase.messaging");
  }
}