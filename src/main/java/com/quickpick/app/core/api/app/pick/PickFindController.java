package com.quickpick.app.core.api.app.pick;

import com.quickpick.app.core.api.request.ApiRequestBody;
import com.quickpick.app.core.api.response.ApiResponse;
import com.quickpick.app.core.api.security.app.AppEndpoint;
import com.quickpick.app.core.api.security.app.AppRestController;
import com.quickpick.app.core.pick.Pick;
import com.quickpick.app.core.pick.PickRepository;
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
public final class PickFindController extends AppRestController {
  private final PickRepository pickRepository;

  private PickFindController(
    @Qualifier("authenticationKey") Key authenticationKey,
    UserRepository userRepository, PickRepository pickRepository
  ) {
    super(authenticationKey, userRepository);
    this.pickRepository = pickRepository;
  }

  @AppEndpoint
  @RequestMapping(path = "/pick/find/", method = RequestMethod.POST)
  public CompletableFuture<ApiResponse> findPick(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = ApiRequestBody.of(payload, response);
    var pickId = body.getUUID("pick_id");
    return findUser(request)
      .thenCompose(user -> pickRepository.findById(pickId)
        .thenApply(entry -> entry
          .map(pick -> deletePick(user, pick))
          .orElse(ApiResponse.error(1000, "Pick not found"))));
  }

  private ApiResponse deletePick(
    User user, Pick pick
  ) {
    var hasPermission = pick.recipients().stream()
      .anyMatch(recipient -> recipient.recipientId().equals(user.id()));
    if (!hasPermission) {
      return ApiResponse.error(1001, "Insufficient permissions");
    }
    return ApiResponse.success(pick.information(user.id()));
  }
}