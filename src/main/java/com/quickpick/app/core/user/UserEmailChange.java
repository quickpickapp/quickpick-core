package com.quickpick.app.core.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.UUID;

@Entity
@Table(name = "user_email_changes")
@Getter
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(staticName = "create")
public final class UserEmailChange {
  @Id
  @Column(name = "user_id", nullable = false, unique = true)
  private UUID userId;
  @Column(name = "new_email", nullable = false)
  private String newEmail;
  @Column(name = "change_token", nullable = false)
  private String changeToken;
}
