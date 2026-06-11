package com.quickpick.app.core.group;

import com.quickpick.app.core.database.DatabaseRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GroupRepository extends DatabaseRepository<Group, UUID> {
}