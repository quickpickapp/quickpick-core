package com.quickpick.app.core.api.app.friendship;

import com.quickpick.app.core.api.request.ApiRequestBody;
import com.quickpick.app.core.api.response.ApiResponse;
import com.quickpick.app.core.api.security.app.AppEndpoint;
import com.quickpick.app.core.api.security.app.AppRestController;
import com.quickpick.app.core.friendship.FriendshipRepository;
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
public final class FriendshipDeleteController extends AppRestController {
  private final FriendshipRepository friendshipRepository;

  private FriendshipDeleteController(
    @Qualifier("authenticationKey") Key authenticationKey,
    UserRepository userRepository, FriendshipRepository friendshipRepository
  ) {
    super(authenticationKey, userRepository);
    this.friendshipRepository = friendshipRepository;
  }

  @AppEndpoint
  @RequestMapping(path = "/friendship/delete/", method = RequestMethod.POST)
  public CompletableFuture<ApiResponse> deleteFriendship(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = ApiRequestBody.of(payload, response);
    var friendId = body.getUUID("friend_id");
    return findUser(request)
      .thenCompose(user -> friendshipRepository.findByPair(user.id(), friendId)
        .thenCompose(entry -> entry
          .map(friendship -> friendshipRepository.delete(friendship)
            .thenApply(_ -> ApiResponse.success()))
          .orElse(ApiResponse.error(1000, "Friendship not found").future())));
  }
}