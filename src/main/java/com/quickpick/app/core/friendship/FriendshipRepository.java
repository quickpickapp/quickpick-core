package com.quickpick.app.core.friendship;

import com.quickpick.app.core.database.DatabaseRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Repository
public interface FriendshipRepository extends DatabaseRepository<Friendship, UUID> {
  @Async
  CompletableFuture<List<Friendship>> findByInvitorIdOrAcceptorId(UUID invitorId, UUID acceptorId);

  default CompletableFuture<List<Friendship>> findAllByUserId(UUID userId) {
    return findByInvitorIdOrAcceptorId(userId, userId);
  }
}