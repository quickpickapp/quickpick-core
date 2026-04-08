package com.quickpick.app.core.apple;

import com.quickpick.app.core.database.DatabaseRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Repository
public interface AppleAuthenticationRepository extends DatabaseRepository<AppleAuthentication, UUID> {
  @Async
  CompletableFuture<List<AppleAuthentication>> findByInvitorIdOrAcceptorId(UUID invitorId, UUID acceptorId);

  default CompletableFuture<List<AppleAuthentication>> findAllByUserId(UUID userId) {
    return findByInvitorIdOrAcceptorId(userId, userId);
  }
}