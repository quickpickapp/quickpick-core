package com.quickpick.app.core.api.app.pick;

import com.quickpick.app.core.api.response.ApiResponse;
import com.quickpick.app.core.api.security.app.AppEndpoint;
import com.quickpick.app.core.api.security.app.AppRestController;
import com.quickpick.app.core.pick.PickRepository;
import com.quickpick.app.core.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Key;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
public final class PickListController extends AppRestController {
  private final PickRepository pickRepository;

  private PickListController(
    @Qualifier("authenticationKey") Key authenticationKey,
    UserRepository userRepository, PickRepository pickRepository
  ) {
    super(authenticationKey, userRepository);
    this.pickRepository = pickRepository;
  }

  @AppEndpoint
  @RequestMapping(path = "/pick/list/", method = RequestMethod.GET)
  public CompletableFuture<ApiResponse> findPick(
    HttpServletRequest request
  ) {
    return findUser(request)
      .thenCompose(user -> pickRepository.findAllByRecipientId(user.id())
        .thenApply(picks -> ApiResponse.success(Map.of("picks",
          picks.stream().map(pick -> pick.information(user.id())).toList()))));
  }
}