package com.quickpick.app.core.api.app.friendship;

import com.quickpick.app.core.api.response.ApiResponse;
import com.quickpick.app.core.api.security.app.AppEndpoint;
import com.quickpick.app.core.api.security.app.AppRestController;
import com.quickpick.app.core.friendship.invitation.Invitation;
import com.quickpick.app.core.friendship.invitation.InvitationRepository;
import com.quickpick.app.core.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Key;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
public final class FriendshipInviteController extends AppRestController {
  private final InvitationRepository invitationRepository;

  private FriendshipInviteController(
    @Qualifier("authenticationKey") Key authenticationKey,
    UserRepository userRepository, InvitationRepository invitationRepository
  ) {
    super(authenticationKey, userRepository);
    this.invitationRepository = invitationRepository;
  }

  @AppEndpoint
  @RequestMapping(path = "/friendship/invite/", method = RequestMethod.GET)
  public CompletableFuture<ApiResponse> createFriendshipInvitation(
    HttpServletRequest request
  ) {
    return invitationRepository.generateAvailableId(UUID::randomUUID)
      .thenApply(id -> Invitation.create(id, findUserId(request),
        System.currentTimeMillis(), -1))
      .thenCompose(invitationRepository::save)
      .thenApply(invitation -> ApiResponse.success(
        Map.of("invitation_id", invitation.id())));
  }
}