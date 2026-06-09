package com.quickpick.app.core.api.app.friendship.invitation;

import com.quickpick.app.core.api.request.ApiRequestBody;
import com.quickpick.app.core.api.response.ApiResponse;
import com.quickpick.app.core.api.security.app.AppEndpoint;
import com.quickpick.app.core.api.security.app.AppRestController;
import com.quickpick.app.core.friendship.Friendship;
import com.quickpick.app.core.friendship.FriendshipRepository;
import com.quickpick.app.core.friendship.invitation.Invitation;
import com.quickpick.app.core.friendship.invitation.InvitationRepository;
import com.quickpick.app.core.notification.NotificationFactory;
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
public final class InvitationAcceptController extends AppRestController {
  private final FriendshipRepository friendshipRepository;
  private final InvitationRepository invitationRepository;
  private final NotificationFactory notificationFactory;

  private InvitationAcceptController(
    @Qualifier("authenticationKey") Key authenticationKey,
    UserRepository userRepository, FriendshipRepository friendshipRepository,
    InvitationRepository invitationRepository,
    NotificationFactory notificationFactory
  ) {
    super(authenticationKey, userRepository);
    this.friendshipRepository = friendshipRepository;
    this.invitationRepository = invitationRepository;
    this.notificationFactory = notificationFactory;
  }

  @AppEndpoint
  @RequestMapping(path = "/friendship/invitation/accept/", method = RequestMethod.POST)
  public CompletableFuture<ApiResponse> acceptFriendshipInvitation(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = ApiRequestBody.of(payload, response);
    var invitationId = body.getUUID("invitation_id");
    return findUser(request)
      .thenCompose(user -> invitationRepository.findById(invitationId)
        .thenCompose(entry -> entry
          .map(invitation -> acceptFriendshipInvitation(user, invitation)
            .thenApply(_ -> ApiResponse.success()))
          .orElse(ApiResponse.error(1000).future())));
  }

  private CompletableFuture<ApiResponse> acceptFriendshipInvitation(
    User user, Invitation invitation
  ) {
    if (!invitation.inviteeId().equals(user.id())) {
      return ApiResponse.error(1001).future();
    }
    sendAcceptNotification(user, invitation);
    invitationRepository.delete(invitation);
    return friendshipRepository.generateAvailableId(UUID::randomUUID)
      .thenCompose(id -> friendshipRepository.save(Friendship.create(id,
        invitation.inviterId(), user.id(), System.currentTimeMillis())))
      .thenApply(_ -> ApiResponse.success());
  }

  private void sendAcceptNotification(
    User user, Invitation invitation
  ) {
    notificationFactory
      .create("friendship.invitation.accept.notification", user.name())
      .sendUserById(invitation.inviterId());
  }
}