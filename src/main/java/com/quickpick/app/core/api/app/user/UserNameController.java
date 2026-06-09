package com.quickpick.app.core.api.app.user;

import com.quickpick.app.core.api.request.ApiRequestBody;
import com.quickpick.app.core.api.response.ApiResponse;
import com.quickpick.app.core.api.security.app.AppEndpoint;
import com.quickpick.app.core.api.security.app.AppRestController;
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
public final class UserNameController extends AppRestController {
  private UserNameController(
    @Qualifier("authenticationKey") Key authenticationKey,
    UserRepository userRepository
  ) {
    super(authenticationKey, userRepository);
  }

  @AppEndpoint
  @RequestMapping(path = "/user/name/change/", method = RequestMethod.POST)
  public CompletableFuture<ApiResponse> changeUserName(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = ApiRequestBody.of(payload, response);
    var name = body.getSanitizedString("name");
    return findUser(request)
      .thenCompose(user -> changeUserName(user, name));
  }

  private CompletableFuture<ApiResponse> changeUserName(User user, String name) {
    user.changeName(name);
    return userRepository().save(user).thenApply(_ -> ApiResponse.success());
  }
}