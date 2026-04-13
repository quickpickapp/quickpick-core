package com.quickpick.app.core.statistic.installation;

import com.quickpick.app.core.database.DatabaseRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AppInstallationRepository extends DatabaseRepository<AppInstallation, UUID> {
}