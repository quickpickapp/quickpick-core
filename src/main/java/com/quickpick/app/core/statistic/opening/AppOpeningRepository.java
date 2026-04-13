package com.quickpick.app.core.statistic.opening;

import com.quickpick.app.core.database.DatabaseRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AppOpeningRepository extends DatabaseRepository<AppOpening, UUID> {
}