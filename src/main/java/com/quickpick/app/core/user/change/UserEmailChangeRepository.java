package com.quickpick.app.core.user.change;

import com.quickpick.app.core.database.DatabaseRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserEmailChangeRepository extends DatabaseRepository<UserEmailChange, UUID> {
}