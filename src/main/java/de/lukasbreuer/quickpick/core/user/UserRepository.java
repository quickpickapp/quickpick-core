package de.lukasbreuer.quickpick.core.user;

import de.lukasbreuer.quickpick.core.database.DatabaseRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Repository
public interface UserRepository extends DatabaseRepository<User, UUID> {
  @Async
  CompletableFuture<Optional<User>> findByEmail(String email);

  @Async
  CompletableFuture<Boolean> existsByEmail(String email);
}