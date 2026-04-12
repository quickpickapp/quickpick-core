package com.quickpick.app.core.pick;

import com.quickpick.app.core.database.DatabaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Repository
public interface PickRepository extends DatabaseRepository<Pick, UUID> {
  @Query("SELECT p FROM Pick p JOIN p.recipients r WHERE r.recipientId = :userId")
  CompletableFuture<List<Pick>> findAllByRecipientId(@Param("userId") UUID userId);
}