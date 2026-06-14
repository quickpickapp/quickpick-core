package com.quickpick.app.core.pick;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
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
  @Column(name = "creator_id", nullable = false, updatable = false)
  private UUID creatorId;
  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private PickType type;
  @Column(name = "nonce", nullable = false, columnDefinition = "TEXT")
  private String nonce;
  @Column(name = "ciphertext", nullable = false, columnDefinition = "TEXT")
  private String ciphertext;
  @Column(name = "tag", nullable = false, columnDefinition = "TEXT")
  private String tag;
  @ElementCollection
  @CollectionTable(
    name = "pick_recipients",
    joinColumns = @JoinColumn(name = "pick_id")
  )
  private List<PickRecipient> recipients;
  @Column(name = "opened_by", nullable = false)
  private List<UUID> openedBy;
  @Column(name = "created_at", nullable = false, updatable = false)
  private long createdAt;
  @Column(name = "expires_at", nullable = false)
  private long expiresAt;

  public void openBy(UUID userId) {
    openedBy.add(userId);
  }
}
