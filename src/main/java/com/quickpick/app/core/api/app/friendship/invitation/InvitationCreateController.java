package com.quickpick.app.core.api.app.friendship.invitation;

import com.quickpick.app.core.api.request.ApiRequestBody;
import com.quickpick.app.core.api.response.ApiResponse;
import com.quickpick.app.core.api.security.app.AppEndpoint;
import com.quickpick.app.core.api.security.app.AppRestController;
import com.quickpick.app.core.friendship.Friendship;
import com.quickpick.app.core.friendship.FriendshipRepository;
import com.quickpick.app.core.friendship.invitation.Invitation;
import com.quickpick.app.core.friendship.invitation.InvitationRepository;
import com.quickpick.app.core.locale.LocaleString;
import com.quickpick.app.core.notification.NotificationFactory;
import com.quickpick.app.core.pick.PickRecipient;
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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
public final class InvitationCreateController extends AppRestController {
  private final FriendshipRepository friendshipRepository;
  private final InvitationRepository invitationRepository;
  private final NotificationFactory notificationFactory;

  private InvitationCreateController(
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
  @RequestMapping(path = "/friendship/invitation/create/", method = RequestMethod.POST)
  public CompletableFuture<ApiResponse> createFriendshipInvitation(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = ApiRequestBody.of(payload, response);
    var inviteeId = UUID.fromString(body.getString("target"));
    return findUser(request).thenCompose(inviter ->
      createFriendshipInvitation(inviter, inviteeId));
  }

  private CompletableFuture<ApiResponse> createFriendshipInvitation(
    User inviter, UUID inviteeId
  ) {
    if (inviter.id().equals(inviteeId)) {
      return ApiResponse.error(1000).future();
    }
    return friendshipRepository.findByPair(inviter.id(), inviteeId)
      .thenCompose(friendship -> createFriendshipInvitation(inviter, inviteeId,
        friendship));
  }

  private CompletableFuture<ApiResponse> createFriendshipInvitation(
    User inviter, UUID inviteeId, Optional<Friendship> friendship
  ) {
    if (friendship.isPresent()) {
      return ApiResponse.error(1001).future();
    }
    return invitationRepository.findByInviterIdAndInviteeId(inviter.id(), inviteeId)
      .thenApply(Optional::isPresent)
      .thenCompose(existing -> createFriendshipInvitation(inviter, inviteeId,
        existing));
  }

  private CompletableFuture<ApiResponse> createFriendshipInvitation(
    User inviter, UUID inviteeId, boolean invitationExists
  ) {
    if (invitationExists) {
      return ApiResponse.error(1002).future();
    }
    sendInvitationNotification(inviter, inviteeId);
    return invitationRepository.generateAvailableId(UUID::randomUUID)
      .thenApply(id -> Invitation.create(id, inviter.id(), inviteeId,
        System.currentTimeMillis(), -1))
      .thenCompose(invitationRepository::save)
      .thenApply(invitation -> ApiResponse.success(
        Map.of("invitation_id", invitation.id())));
  }

  private void sendInvitationNotification(
    User inviter, UUID inviteeId
  ) {
    notificationFactory
      .create("friendship.invitation.create.notification", inviter.name())
      .sendUserById(inviteeId);
  }
}