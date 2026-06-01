package com.quickpick.app.core.friendship.invitation;

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
@Table(name = "invitations")
@Getter
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(staticName = "create")
public class Invitation {
  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;
  @Column(name = "inviter_id", nullable = false, updatable = false)
  private UUID inviterId;
  @Column(name = "invitee_id", nullable = false, updatable = false)
  private UUID inviteeId;
  @Column(name = "created_at", nullable = false, updatable = false)
  private long createdAt;
  @Column(name = "expires_at", nullable = false)
  private long expiresAt;
}