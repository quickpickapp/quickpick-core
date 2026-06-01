package com.quickpick.app.core.friendship.invitation;

import com.quickpick.app.core.database.DatabaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Repository
public interface InvitationRepository extends DatabaseRepository<Invitation, UUID> {
  CompletableFuture<Optional<Invitation>> findByInviterIdAndInviteeId(UUID inviterId, UUID inviteeId);

  CompletableFuture<List<Invitation>> findByInviteeId(UUID inviteeId);
}
