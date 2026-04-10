package com.quickpick.app.core.mail;

import com.quickpick.app.core.database.DatabaseRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OutgoingMailRepository extends DatabaseRepository<OutgoingMail, UUID> {
}