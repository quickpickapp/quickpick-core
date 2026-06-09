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
public final class UserLanguageController extends AppRestController {
  private UserLanguageController(
    @Qualifier("authenticationKey") Key authenticationKey,
    UserRepository userRepository
  ) {
    super(authenticationKey, userRepository);
  }

  @AppEndpoint
  @RequestMapping(path = "/user/language/change/", method = RequestMethod.POST)
  public CompletableFuture<ApiResponse> changeUserLanguage(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = ApiRequestBody.of(payload, response);
    var language = body.getString("language");
    return findUser(request)
      .thenCompose(user -> changeUserLanguage(user, language));
  }

  private CompletableFuture<ApiResponse> changeUserLanguage(
    User user, String language
  ) {
    user.changeLanguage(language);
    return userRepository().save(user).thenApply(_ -> ApiResponse.success());
  }
}