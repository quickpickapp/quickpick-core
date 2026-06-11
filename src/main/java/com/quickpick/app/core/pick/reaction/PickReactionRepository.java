package com.quickpick.app.core.pick.reaction;

import com.quickpick.app.core.database.DatabaseRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PickReactionRepository extends DatabaseRepository<PickReaction, UUID> {
}