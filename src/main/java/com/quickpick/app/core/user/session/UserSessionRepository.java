package com.quickpick.app.core.user.session;

import com.quickpick.app.core.database.DatabaseRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Repository
public interface UserSessionRepository extends DatabaseRepository<UserSession, UUID> {
  @Async
  CompletableFuture<List<UserSession>> findByMemberIdAndStatus(
    UUID memberId, UserSessionStatus status);
}