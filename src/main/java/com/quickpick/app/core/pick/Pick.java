package com.quickpick.app.core.pick;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "picks")
@Getter
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(staticName = "create")
public class Pick {
  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;
  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private PickType type;
  @Column(name = "content", nullable = false)
  private String content;
  @ElementCollection
  @CollectionTable(
    name = "pick_recipients",
    joinColumns = @JoinColumn(name = "pick_id")
  )
  @MapKeyColumn(name = "recipient_id")
  @Column(name = "decryption_key")
  private Map<UUID, String> decryption_keys;
  @Column(name = "created_at", nullable = false, updatable = false)
  private long createdAt;
  @Column(name = "expires_at", nullable = false)
  private long expiresAt;
}
