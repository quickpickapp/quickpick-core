package com.quickpick.app.core.api.app.pick;

import com.google.common.collect.Maps;
import com.quickpick.app.core.api.request.ApiRequestBody;
import com.quickpick.app.core.api.response.ApiResponse;
import com.quickpick.app.core.api.security.app.AppEndpoint;
import com.quickpick.app.core.api.security.app.AppRestController;
import com.quickpick.app.core.pick.Pick;
import com.quickpick.app.core.pick.PickRepository;
import com.quickpick.app.core.user.User;
import com.quickpick.app.core.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Key;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
public final class PickOpenController extends AppRestController {
  private final PickRepository pickRepository;

  private PickOpenController(
    @Qualifier("authenticationKey") Key authenticationKey,
    UserRepository userRepository, PickRepository pickRepository
  ) {
    super(authenticationKey, userRepository);
    this.pickRepository = pickRepository;
  }

  @AppEndpoint
  @RequestMapping(path = "/pick/open/", method = RequestMethod.POST)
  public CompletableFuture<ApiResponse> openPick(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = ApiRequestBody.of(payload, response);
    var pickId = body.getUUID("pick_id");
    return findUser(request)
      .thenCompose(user -> pickRepository.findById(pickId)
        .thenCompose(entry -> entry
          .map(pick -> openPick(user, pick))
          .orElse(ApiResponse.error(1000, "Pick not found").future())));
  }

  private CompletableFuture<ApiResponse> openPick(
    User user, Pick pick
  ) {
    var hasPermission = pick.recipients().stream()
      .anyMatch(recipient -> recipient.recipientId().equals(user.id()));
    if (!hasPermission) {
      return ApiResponse.error(1001, "Insufficient permissions").future();
    }
    return userRepository().findById(pick.creatorId())
      .thenApply(creator -> ApiResponse.success(
        assemblePickInformation(pick, user.id(), creator.get())));
  }

  public Map<String, Object> assemblePickInformation(
    Pick pick, UUID recipientId, User creator
  ) {
    var information = Maps.<String, Object>newHashMap();
    information.put("id", pick.id());
    information.put("creator_id", creator.id());
    information.put("creator_public_key", creator.publicKey());
    information.put("creator_name", creator.name());
    information.put("type", pick.type());
    information.put("nonce", pick.nonce());
    information.put("ciphertext", pick.ciphertext());
    information.put("tag", pick.tag());
    var decryptionKey = pick.recipients().stream()
      .filter(recipient -> recipient.recipientId().equals(recipientId))
      .findFirst().get().decryptionKey();
    information.put("decryption_key", decryptionKey);
    information.put("created_at", pick.createdAt());
    information.put("expires_at", pick.expiresAt());
    return information;
  }
}