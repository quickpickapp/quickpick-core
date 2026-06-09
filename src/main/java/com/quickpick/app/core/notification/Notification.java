package com.quickpick.app.core.notification;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Maps;
import com.quickpick.app.core.iterator.AsyncIterator;
import com.quickpick.app.core.locale.LocaleString;
import com.quickpick.app.core.locale.Translation;
import com.quickpick.app.core.log.Log;
import com.quickpick.app.core.user.User;
import com.quickpick.app.core.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor(staticName = "create")
public final class Notification {
  private final Log log;
  private final FirebaseConfiguration firebaseConfiguration;
  private final GoogleCredentials googleCredentials;
  private final UserRepository userRepository;
  private final Translation translation;
  private final LocaleString title;
  private final LocaleString body;
  private final Map<String, Object> data;

  private static final String FIREBASE_URL =
    "https://fcm.googleapis.com/v1/projects/%s/messages:send";

  public void sendUsersByIds(List<UUID> receiverIds) {
    AsyncIterator.execute(receiverIds, userRepository::findById)
      .thenAccept(receivers -> sendUsers(receivers.stream()
        .filter(Optional::isPresent).map(Optional::get).toList()));
  }

  public void sendUsers(List<User> receivers) {
    for (var receiver : receivers) {
      sendUser(receiver);
    }
  }

  public void sendUserById(UUID receiverId) {
    userRepository.findById(receiverId)
      .thenAccept(receiver -> receiver.ifPresent(this::sendUser));
  }

  public void sendUser(User receiver) {
    try {
      HttpClient.newHttpClient().sendAsync(createRequest(receiver),
        HttpResponse.BodyHandlers.ofByteArray());
    } catch (Exception exception) {
      log.processError(exception);
    }
  }

  private HttpRequest createRequest(User receiver) throws Exception{
    googleCredentials.refreshIfExpired();
    var token = googleCredentials.getAccessToken().getTokenValue();
    var url = String.format(FIREBASE_URL, firebaseConfiguration.projectId());
    var requestBody = new JSONObject(createPayload(receiver));
    return HttpRequest.newBuilder().uri(URI.create(url))
      .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
      .setHeader("Content-Type", "application/json")
      .setHeader("Authorization", "Bearer " + token)
      .build();
  }

  private Map<String, Object> createPayload(
    User receiver
  ) {
    var translatedTitle = translation.translateUser(receiver, title);
    var translatedBody = translation.translateUser(receiver, body);
    var payload = Maps.<String, Object>newHashMap();
    var message = Maps.<String, Object>newHashMap();
    message.put("token", receiver.firebaseToken());
    var content = Maps.<String, Object>newHashMap();
    content.put("title", translatedTitle);
    content.put("body", translatedBody);
    content.putAll(data);
    message.put("data", content);
    message.put("android", createAndroidPayload());
    message.put("apns", createIOSPayload(translatedTitle, translatedBody));
    payload.put("message", message);
    return payload;
  }

  private Map<String, Object> createAndroidPayload() {
    var android = Maps.<String, Object>newHashMap();
    android.put("priority", "HIGH");
    var notification = Maps.<String, Object>newHashMap();
    notification.put("channel_id", "quickpick");
    android.put("notification", notification);
    return android;
  }

  private Map<String, Object> createIOSPayload(String title, String body) {
    var apns = Maps.<String, Object>newHashMap();
    var headers = Maps.<String, Object>newHashMap();
    headers.put("apns-priority", "10");
    apns.put("headers", headers);
    var payload = Maps.<String, Object>newHashMap();
    var aps = Maps.<String, Object>newHashMap();
    var alert = Maps.<String, Object>newHashMap();
    alert.put("title", title);
    alert.put("body", body);
    aps.put("alert", alert);
    payload.put("aps", aps);
    apns.put("payload", payload);
    return apns;
  }
}
