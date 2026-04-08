package com.quickpick.app.core.pick;

import com.quickpick.app.core.database.DatabaseRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PickRepository extends DatabaseRepository<Pick, UUID> {
}