package com.quickpick.app.core.api.app.friendship;

import com.quickpick.app.core.api.response.ApiResponse;
import com.quickpick.app.core.api.security.app.AppEndpoint;
import com.quickpick.app.core.api.security.app.AppRestController;
import com.quickpick.app.core.friendship.Friendship;
import com.quickpick.app.core.friendship.FriendshipRepository;
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
public final class FriendshipListController extends AppRestController {
  private final FriendshipRepository friendshipRepository;

  private FriendshipListController(
    @Qualifier("authenticationKey") Key authenticationKey,
    UserRepository userRepository, FriendshipRepository friendshipRepository
  ) {
    super(authenticationKey, userRepository);
    this.friendshipRepository = friendshipRepository;
  }

  @AppEndpoint
  @RequestMapping(path = "/friendship/list/", method = RequestMethod.GET)
  public CompletableFuture<ApiResponse> listFriendships(
    HttpServletRequest request
  ) {
    return findUser(request)
      .thenCompose(user -> friendshipRepository.findAllByUserId(user.id())
        .thenCompose(friendships -> AsyncIterator.execute(friendships,
          friendship -> findFriend(user, friendship)))
        .thenApply(friends -> ApiResponse.success(Map.of("friendships",
          friends.stream().map(this::assembleFriendshipInformation).toList()))));
  }

  private Map<String, Object> assembleFriendshipInformation(User friend) {
    return Map.of("id", friend.id(),  "name", friend.name(),
      "public_key", friend.publicKey());
  }

  private CompletableFuture<User> findFriend(User user, Friendship friendship) {
    var friendId = friendship.invitorId().equals(user.id()) ?
      friendship.acceptorId() : friendship.invitorId();
    return userRepository().findById(friendId).thenApply(Optional::get);
  }
}