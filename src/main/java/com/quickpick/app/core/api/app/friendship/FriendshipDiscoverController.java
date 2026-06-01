package com.quickpick.app.core.api.app.friendship;

import com.quickpick.app.core.api.request.ApiRequestBody;
import com.quickpick.app.core.api.response.ApiResponse;
import com.quickpick.app.core.api.security.app.AppEndpoint;
import com.quickpick.app.core.api.security.app.AppRestController;
import com.quickpick.app.core.friendship.FriendshipRepository;
import com.quickpick.app.core.friendship.invitation.InvitationRepository;
import com.quickpick.app.core.iterator.AsyncIterator;
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
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@RestController
public final class FriendshipDiscoverController extends AppRestController {
  private final FriendshipRepository friendshipRepository;
  private final InvitationRepository invitationRepository;

  private FriendshipDiscoverController(
    @Qualifier("authenticationKey") Key authenticationKey,
    UserRepository userRepository, FriendshipRepository friendshipRepository,
    InvitationRepository invitationRepository
  ) {
    super(authenticationKey, userRepository);
    this.friendshipRepository = friendshipRepository;
    this.invitationRepository = invitationRepository;
  }

  @AppEndpoint
  @RequestMapping(path = "/friendship/discover/", method = RequestMethod.POST)
  public CompletableFuture<ApiResponse> discoverFriendship(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = ApiRequestBody.of(payload, response);
    var contacts = body.<String>getList("contacts");
    return findUser(request).thenCompose(explorer ->
      AsyncIterator.execute(contacts, contact -> checkContact(explorer, contact))
        .thenApply(suggestions -> suggestions.stream()
          .filter(Objects::nonNull).toList())
        .thenApply(suggestions -> ApiResponse.success(
          Map.of("suggestions", suggestions))));
  }

  private CompletableFuture<Map<String, Object>> checkContact(
    User explorer, String contact
  ) {
    if (contact.equals(explorer.phoneNumber())) {
      return CompletableFuture.completedFuture(null);
    }
    return userRepository().findByPhoneNumber(contact)
      .thenCompose(contactUser -> checkContact(explorer, contactUser.orElse(null)));
  }

  private CompletableFuture<Map<String, Object>> checkContact(
    User explorer, User suggestion
  ) {
    if (suggestion == null) {
      return CompletableFuture.completedFuture(null);
    }
    return friendshipRepository.findByPair(explorer.id(), suggestion.id())
      .thenCompose(friendship -> invitationRepository
        .findByInviterIdAndInviteeId(explorer.id(), suggestion.id())
        .thenApply(invitation -> checkContact(suggestion,
          friendship.isPresent(), invitation.isPresent())));
  }

  private Map<String, Object> checkContact(
    User suggestion, boolean friendshipExists, boolean invitationExists
  ) {
    if (friendshipExists || invitationExists) {
      return null;
    }
    return Map.of("id", suggestion.id(), "name", suggestion.name());
  }
}