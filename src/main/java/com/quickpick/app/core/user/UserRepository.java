package com.quickpick.app.core.user;

import com.quickpick.app.core.database.DatabaseRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Repository
public interface UserRepository extends DatabaseRepository<User, UUID> {
}