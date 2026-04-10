package com.quickpick.app.core.user.device;

import com.quickpick.app.core.database.DatabaseRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserDeviceRepository extends DatabaseRepository<UserDevice, UUID> {
}