package com.quickpick.app.core.notification;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Maps;
import com.quickpick.app.core.log.Log;
import com.quickpick.app.core.user.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class NotificationFactory {
  private final Log log;
  private final FirebaseConfiguration notificationConfiguration;
  private final GoogleCredentials googleCredentials;
  private final UserRepository userRepository;

  public Notification create(String title, String body) {
    return create(title, body, Maps.newHashMap());
  }

  public Notification create(
    String title, String body, Map<String, Object> data
  ) {
    return Notification.create(log, notificationConfiguration,
      googleCredentials, userRepository, title, body, data);
  }
}
