package com.quickpick.app.core.user.verification;

import com.quickpick.app.core.database.DatabaseRepository;
import com.quickpick.app.core.user.change.UserEmailChange;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserVerificationRepository extends DatabaseRepository<UserVerification, UUID> {
}