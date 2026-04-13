package com.quickpick.app.core.friendship.invitation;

import com.quickpick.app.core.database.DatabaseRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InvitationRepository extends DatabaseRepository<Invitation, UUID> {
}
