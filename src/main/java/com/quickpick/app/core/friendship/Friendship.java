package com.quickpick.app.core.friendship;

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
@Table(name = "friendships")
@Getter
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(staticName = "create")
public class Friendship {
  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;
  @Column(name = "invitor_id", nullable = false)
  private UUID invitorId;
  @Column(name = "acceptor_id", nullable = false)
  private UUID acceptorId;
  @Column(name = "created_at", nullable = false, updatable = false)
  private long createdAt;
}