package com.quickpick.app.core.notification;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class NotificationFactory {
  private final FirebaseConfiguration notificationConfiguration;
  private final GoogleCredentials googleCredentials;

  public Notification create(String receiver, String title, String body) {
    return create(receiver, title, body, Maps.newHashMap());
  }

  public Notification create(
    String receiver, String title, String body, Map<String, Object> data
  ) {
    return Notification.create(notificationConfiguration, googleCredentials,
      receiver, title, body, data);
  }
}
