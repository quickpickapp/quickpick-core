package com.quickpick.app.core.api.app.authentication;

import com.google.common.collect.Lists;
import com.quickpick.app.core.api.request.ApiRequestBody;
import com.quickpick.app.core.iterator.AsyncIterator;
import com.quickpick.app.core.user.User;
import com.quickpick.app.core.user.UserRepository;
import com.quickpick.app.core.user.device.UserDevice;
import com.quickpick.app.core.user.device.UserDeviceRepository;
import com.quickpick.app.core.user.verification.UserVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Configuration
@RequiredArgsConstructor
public class AuthenticationSignup {
  private final UserRepository userRepository;
  private final UserDeviceRepository userDeviceRepository;
  private final UserVerificationRepository userVerificationRepository;

  public CompletableFuture<User> signupUser(String email, ApiRequestBody body) {
    return generateAvailableUserId()
      .thenCompose(userId -> signupUser(userId, email, body));
  }

  public CompletableFuture<User> signupUser(
    UUID userId, String email, ApiRequestBody body
  ) {
    var legalAccepted = body.getBoolean("legalAccepted");
    if (!legalAccepted) {
      return CompletableFuture.completedFuture(null);
    }
    return userDeviceRepository.generateAvailableId(UUID::randomUUID)
      .thenCompose(deviceId -> signupUser(userId, body.getSanitizedString("name"),
        email, body.getString("public_key"), legalAccepted, deviceId,
        body.getString("device_id"), body.getString("operating_system"),
        body.getString("operating_system_version"),
        body.getString("device_brand"), body.getString("device_model"),
        body.getString("device_name")));
  }

  private CompletableFuture<User> signupUser(
    UUID id, String name, String email, String publicKey, boolean compliant,
    UUID deviceId, String publicDeviceId, String operatingSystem, String operatingSystemVersion,
    String deviceBrand, String deviceModel, String deviceName
  ) {
    var processes = Lists.<CompletableFuture<Void>>newArrayList();
    var user = User.create(id, name, email, compliant, publicKey,
      System.currentTimeMillis());
    var device = UserDevice.create(deviceId, id, publicDeviceId, operatingSystem,
      operatingSystemVersion, deviceBrand, deviceModel, deviceName);
    processes.add(userRepository.save(user).thenApply(_ -> null));
    processes.add(userDeviceRepository.save(device).thenApply(_ -> null));
    return AsyncIterator.execute(processes, process -> process)
      .thenApply(_ -> user);
  }

  public CompletableFuture<UUID> generateAvailableUserId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    userRepository.existsById(id).thenCompose(userExists -> userExists ?
      generateAvailableUserId().thenApply(futureResponse::complete) :
      userVerificationRepository.existsById(id)
        .thenCompose(verificationExists -> verificationExists ?
          generateAvailableUserId().thenApply(futureResponse::complete) :
          CompletableFuture.completedFuture(futureResponse.complete(id))));
    return futureResponse;
  }
}
