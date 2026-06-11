package com.quickpick.app.core.pick.reaction;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.UUID;

@Entity
@Table(name = "pick_reactions")
@Getter
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(staticName = "create")
public class PickReaction {
  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;
  @Column(name = "pick_id", nullable = false, updatable = false)
  private UUID pickId;
  @Column(name = "reactor_id", nullable = false, updatable = false)
  private UUID reactorId;
  @Column(name = "reaction", nullable = false, updatable = false)
  private String reaction;
  @Column(name = "created_at", nullable = false, updatable = false)
  private long createdAt;
}