package com.quickpick.app.core.api.app.pick;

import com.quickpick.app.core.api.request.ApiRequestBody;
import com.quickpick.app.core.api.response.ApiResponse;
import com.quickpick.app.core.api.security.app.AppEndpoint;
import com.quickpick.app.core.api.security.app.AppRestController;
import com.quickpick.app.core.notification.NotificationFactory;
import com.quickpick.app.core.pick.Pick;
import com.quickpick.app.core.pick.PickRepository;
import com.quickpick.app.core.pick.reaction.PickReaction;
import com.quickpick.app.core.pick.reaction.PickReactionRepository;
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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
public final class PickReactController extends AppRestController {
  private final PickRepository pickRepository;
  private final PickReactionRepository pickReactionRepository;
  private final NotificationFactory notificationFactory;

  private PickReactController(
    @Qualifier("authenticationKey") Key authenticationKey,
    UserRepository userRepository, PickRepository pickRepository,
    PickReactionRepository pickReactionRepository,
    NotificationFactory notificationFactory
  ) {
    super(authenticationKey, userRepository);
    this.pickRepository = pickRepository;
    this.pickReactionRepository = pickReactionRepository;
    this.notificationFactory = notificationFactory;
  }

  @AppEndpoint
  @RequestMapping(path = "/pick/react/", method = RequestMethod.POST)
  public CompletableFuture<ApiResponse> reactToPick(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = ApiRequestBody.of(payload, response);
    var pickId = body.getUUID("pick_id");
    var reaction = body.getSanitizedString("reaction");
    return findUser(request)
      .thenCompose(user -> pickRepository.findById(pickId)
        .thenCompose(entry -> entry
          .map(pick -> reactToPick(user, pick, reaction))
          .orElse(ApiResponse.error(1000, "Pick not found").future())));
  }

  private CompletableFuture<ApiResponse> reactToPick(
    User user, Pick pick, String reaction
  ) {
    var isRecipient = pick.recipients().stream()
      .anyMatch(recipient -> recipient.recipientId().equals(user.id()));
    if (!isRecipient) {
      return ApiResponse.error(1001, "Not a pick recipient").future();
    }
    notificationFactory.create("pick.react.notification", user.name(), reaction)
      .sendUserById(pick.creatorId());
    return pickReactionRepository.generateAvailableId(UUID::randomUUID)
      .thenApply(id -> PickReaction.create(id, pick.id(), user.id(), reaction,
        System.currentTimeMillis()))
      .thenCompose(pickReactionRepository::save)
      .thenApply(_ -> ApiResponse.success());
  }
}