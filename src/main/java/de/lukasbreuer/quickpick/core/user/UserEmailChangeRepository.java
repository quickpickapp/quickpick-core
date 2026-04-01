package de.lukasbreuer.quickpick.core.user;

import de.lukasbreuer.quickpick.core.database.DatabaseRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserEmailChangeRepository extends DatabaseRepository<UserEmailChange, UUID> {
}