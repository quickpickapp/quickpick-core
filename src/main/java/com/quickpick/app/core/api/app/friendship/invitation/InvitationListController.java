package com.quickpick.app.core.api.app.friendship.invitation;

import com.quickpick.app.core.api.response.ApiResponse;
import com.quickpick.app.core.api.security.app.AppEndpoint;
import com.quickpick.app.core.api.security.app.AppRestController;
import com.quickpick.app.core.friendship.invitation.Invitation;
import com.quickpick.app.core.friendship.invitation.InvitationRepository;
import com.quickpick.app.core.iterator.AsyncIterator;
import com.quickpick.app.core.user.User;
import com.quickpick.app.core.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Key;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
public final class InvitationListController extends AppRestController {
  private final InvitationRepository invitationRepository;

  private InvitationListController(
    @Qualifier("authenticationKey") Key authenticationKey,
    UserRepository userRepository, InvitationRepository invitationRepository
  ) {
    super(authenticationKey, userRepository);
    this.invitationRepository = invitationRepository;
  }

  @AppEndpoint
  @RequestMapping(path = "/friendship/invitation/list/", method = RequestMethod.GET)
  public CompletableFuture<ApiResponse> listFriendshipInvitations(
    HttpServletRequest request
  ) {
    return findUser(request)
      .thenCompose(user -> invitationRepository.findByInviteeId(user.id())
        .thenCompose(invitations -> AsyncIterator.execute(invitations,
          invitation -> userRepository().findById(invitation.inviterId())
            .thenApply(Optional::get)
            .thenApply(inviter -> assembleInvitationInformation(invitation, inviter))))
        .thenApply(invitations -> ApiResponse.success(Map.of("invitations",
          invitations))));
  }

  private Map<String, Object> assembleInvitationInformation(
    Invitation invitation, User inviter
  ) {
    return Map.of("invitation_id", invitation.id(), "inviter_id", inviter.id(),
      "inviter_name", inviter.name());
  }
}