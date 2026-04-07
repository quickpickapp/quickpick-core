package com.quickpick.app.core.statistic;

import com.quickpick.app.core.database.DatabaseRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserJoinRepository extends DatabaseRepository<UserJoin, UUID> {
}