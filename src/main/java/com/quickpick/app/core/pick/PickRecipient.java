package com.quickpick.app.core.pick;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.UUID;

@Embeddable
@Getter
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(staticName = "create")
public class PickRecipient {
  @Column(name = "recipient_id", nullable = false)
  private UUID recipientId;
  @Column(name = "decryption_key", nullable = false)
  private String decryptionKey;
}