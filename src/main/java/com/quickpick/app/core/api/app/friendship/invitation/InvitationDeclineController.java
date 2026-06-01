package com.quickpick.app.core.api.app.friendship.invitation;

import com.quickpick.app.core.api.request.ApiRequestBody;
import com.quickpick.app.core.api.response.ApiResponse;
import com.quickpick.app.core.api.security.app.AppEndpoint;
import com.quickpick.app.core.api.security.app.AppRestController;
import com.quickpick.app.core.friendship.invitation.Invitation;
import com.quickpick.app.core.friendship.invitation.InvitationRepository;
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
import java.util.concurrent.CompletableFuture;

@RestController
public final class InvitationDeclineController extends AppRestController {
  private final InvitationRepository invitationRepository;

  private InvitationDeclineController(
    @Qualifier("authenticationKey") Key authenticationKey,
    UserRepository userRepository, InvitationRepository invitationRepository
  ) {
    super(authenticationKey, userRepository);
    this.invitationRepository = invitationRepository;
  }

  @AppEndpoint
  @RequestMapping(path = "/friendship/invitation/decline/", method = RequestMethod.POST)
  public CompletableFuture<ApiResponse> declineFriendshipInvitation(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = ApiRequestBody.of(payload, response);
    var invitationId = body.getUUID("invitation_id");
    return findUser(request)
      .thenCompose(user -> invitationRepository.findById(invitationId)
        .thenCompose(entry -> entry
          .map(invitation -> declineFriendshipInvitation(user, invitation)
            .thenApply(_ -> ApiResponse.success()))
          .orElse(ApiResponse.error(1000).future())));
  }

  private CompletableFuture<ApiResponse> declineFriendshipInvitation(
    User user, Invitation invitation
  ) {
    if (!invitation.inviteeId().equals(user.id())) {
      return ApiResponse.error(1001).future();
    }
    return invitationRepository.delete(invitation)
      .thenApply(_ -> ApiResponse.success());
  }
}