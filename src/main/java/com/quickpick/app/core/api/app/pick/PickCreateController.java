package com.quickpick.app.core.api.app.pick;

import com.quickpick.app.core.api.request.ApiRequestBody;
import com.quickpick.app.core.api.response.ApiResponse;
import com.quickpick.app.core.api.security.app.AppEndpoint;
import com.quickpick.app.core.api.security.app.AppRestController;
import com.quickpick.app.core.friendship.Friendship;
import com.quickpick.app.core.friendship.FriendshipRepository;
import com.quickpick.app.core.notification.NotificationFactory;
import com.quickpick.app.core.pick.Pick;
import com.quickpick.app.core.pick.PickRecipient;
import com.quickpick.app.core.pick.PickRepository;
import com.quickpick.app.core.pick.PickType;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
public final class PickCreateController extends AppRestController {
  private final PickRepository pickRepository;
  private final FriendshipRepository friendshipRepository;
  private final NotificationFactory notificationFactory;

  private PickCreateController(
    @Qualifier("authenticationKey") Key authenticationKey,
    UserRepository userRepository, PickRepository pickRepository,
    FriendshipRepository friendshipRepository,
    NotificationFactory notificationFactory
  ) {
    super(authenticationKey, userRepository);
    this.pickRepository = pickRepository;
    this.friendshipRepository = friendshipRepository;
    this.notificationFactory = notificationFactory;
  }

  private static final long MINIMUM_PICK_DURATION = 60 * 1000L;

  @AppEndpoint
  @RequestMapping(path = "/pick/create/", method = RequestMethod.POST)
  public CompletableFuture<ApiResponse> createPick(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = ApiRequestBody.of(payload, response);
    var rawType = body.getString("type");
    if (!PickType.isValid(rawType)) {
      return ApiResponse.error(1000, "Wrong pick type").future();
    }
    var type = PickType.valueOf(rawType);
    var duration = body.getLong("duration");
    if (duration < MINIMUM_PICK_DURATION) {
      return ApiResponse.error(1001, "Falling below the minimum duration").future();
    }
    var nonce = body.getString("nonce");
    var ciphertext = body.getString("ciphertext");
    var tag = body.getString("tag");
    var recipients = parseRecipients(body);
    return findUser(request)
      .thenCompose(user -> friendshipRepository.findAllByUserId(user.id())
        .thenCompose(friendships -> createPick(user, friendships, type,
          duration, nonce, ciphertext, tag, recipients)));
  }

  private List<PickRecipient> parseRecipients(ApiRequestBody body) {
    return body.getObjectList("recipients").stream()
      .map(entry -> PickRecipient.create(entry.getUUID("recipient_id"),
        entry.getString("decryption_key")))
      .toList();
  }

  private CompletableFuture<ApiResponse> createPick(
    User user, List<Friendship> friendships, PickType type, long duration,
    String nonce, String ciphertext, String tag, List<PickRecipient> recipients
  ) {
    if (!checkRecipients(user, friendships, recipients)) {
      return ApiResponse.error(1002, "Unknown recipient").future();
    }
    return pickRepository.generateAvailableId(UUID::randomUUID)
      .thenCompose(id -> createPick(user, id, type, duration, nonce, ciphertext,
        tag, recipients));
  }

  private CompletableFuture<ApiResponse> createPick(
    User user, UUID pickId, PickType type, long duration,
    String nonce, String ciphertext, String tag, List<PickRecipient> recipients
  ) {
    var currentTime = System.currentTimeMillis();
    var pick = Pick.create(pickId, user.id(), type, nonce, ciphertext, tag,
      recipients, currentTime, currentTime + duration);
    sendPickNotification(user, recipients);
    return pickRepository.save(pick).thenApply(_ ->
      ApiResponse.success(Map.of("pick_id", pickId)));
  }

  private void sendPickNotification(User sender, List<PickRecipient> recipients) {
    notificationFactory.create("Neuer Pick von " + sender.name(),
        "Du hast einen neuen Pick erhalten. Klicke um zu öffen.")
      .sendUserIds(recipients.stream().map(PickRecipient::recipientId).toList());
  }

  private boolean checkRecipients(
    User user, List<Friendship> friendships, List<PickRecipient> recipients
  ) {
    for (var recipient : recipients) {
      var recipientId = recipient.recipientId();
      if (recipientId.equals(user.id())) {
        return false;
      }
      var isFriend = friendships.stream()
        .anyMatch(friendship -> friendship.invitorId().equals(recipientId) ||
          friendship.acceptorId().equals(recipientId));
      if (!isFriend) {
        return false;
      }
    }
    return true;
  }
}
