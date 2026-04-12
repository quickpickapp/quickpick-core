package com.quickpick.app.core.pick;

import com.google.common.collect.Maps;
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
  @Column(name = "creator_id", nullable = false, updatable = false)
  private UUID creatorId;
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
  private List<PickRecipient> recipients;
  @Column(name = "created_at", nullable = false, updatable = false)
  private long createdAt;
  @Column(name = "expires_at", nullable = false)
  private long expiresAt;

  public Map<String, Object> information(UUID recipientId) {
    var information = Maps.<String, Object>newHashMap();
    information.put("id", id);
    information.put("creator_id", creatorId);
    information.put("type", type);
    information.put("content", content);
    var decryptionKey = recipients.stream()
      .filter(recipient -> recipient.recipientId().equals(recipientId))
      .findFirst().get().decryptionKey();
    information.put("decryption_key", decryptionKey);
    information.put("created_at", createdAt);
    information.put("expires_at", expiresAt);
    return information;
  }
}
