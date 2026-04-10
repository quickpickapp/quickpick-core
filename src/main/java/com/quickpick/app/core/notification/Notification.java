package com.quickpick.app.core.notification;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@RequiredArgsConstructor(staticName = "create")
public final class Notification {
  private final FirebaseConfiguration firebaseConfiguration;
  private final GoogleCredentials googleCredentials;
  private final String receiver;
  private final String title;
  private final String body;
  private final Map<String, Object> data;

  private static final String FIREBASE_URL =
    "https://fcm.googleapis.com/v1/projects/%s/messages:send";

  public void send() throws Exception {
    googleCredentials.refreshIfExpired();
    var token = googleCredentials.getAccessToken().getTokenValue();
    var url = String.format(FIREBASE_URL, firebaseConfiguration.projectId());
    var requestBody = new JSONObject(createPayload());
    var requestBuilder = HttpRequest.newBuilder().uri(URI.create(url))
      .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
      .setHeader("Content-Type", "application/json")
      .setHeader("Authorization", "Bearer " + token)
      .build();
    HttpClient.newHttpClient().sendAsync(requestBuilder,
      HttpResponse.BodyHandlers.ofByteArray());
  }

  private Map<String, Object> createPayload() {
    var payload = Maps.<String, Object>newHashMap();
    var message = Maps.<String, Object>newHashMap();
    message.put("topic", receiver);
    var content = Maps.<String, Object>newHashMap();
    content.put("title", title);
    content.put("body", body);
    content.putAll(data);
    message.put("data", content);
    message.put("android", createAndroidPayload());
    message.put("apns", createIOSPayload());
    payload.put("message", message);
    return payload;
  }

  private Map<String, Object> createAndroidPayload() {
    var android = Maps.<String, Object>newHashMap();
    android.put("priority", "HIGH");
    return android;
  }

  private Map<String, Object> createIOSPayload() {
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
