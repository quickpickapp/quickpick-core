package com.quickpick.app.core.user.session;

public enum UserSessionStatus {
  ACTIVE,
  CLOSED;

  public boolean isActive() {
    return this == ACTIVE;
  }

  public boolean isClosed() {
    return this == CLOSED;
  }
}
