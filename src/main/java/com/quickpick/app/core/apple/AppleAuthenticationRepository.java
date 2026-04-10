package com.quickpick.app.core.apple;

import com.quickpick.app.core.database.DatabaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppleAuthenticationRepository extends DatabaseRepository<AppleAuthentication, String> {
}