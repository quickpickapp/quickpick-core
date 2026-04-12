package com.quickpick.app.core.friendship;

import com.quickpick.app.core.database.DatabaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Repository
public interface FriendshipRepository extends DatabaseRepository<Friendship, UUID> {
  @Async
  CompletableFuture<List<Friendship>> findByInvitorIdOrAcceptorId(UUID invitorId, UUID acceptorId);

  default CompletableFuture<List<Friendship>> findAllByUserId(UUID userId) {
    return findByInvitorIdOrAcceptorId(userId, userId);
  }

  @Query("SELECT f FROM Friendship f WHERE (f.invitorId = :a AND f.acceptorId = :b) OR (f.invitorId = :b AND f.acceptorId = :a)")
  CompletableFuture<Optional<Friendship>> findByPair(@Param("a") UUID userA, @Param("b") UUID userB);
}