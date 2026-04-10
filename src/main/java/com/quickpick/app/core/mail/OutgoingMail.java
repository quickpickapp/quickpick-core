package com.quickpick.app.core.mail;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.UUID;

@Entity
@Table(name = "outgoing_mails")
@Getter
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(staticName = "create")
public final class OutgoingMail {
  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;
  @Column(name = "receiver", nullable = false)
  private String receiver;
  @Column(name = "sender", nullable = false)
  private String sender;
  @Column(name = "time", nullable = false, updatable = false)
  private long time;
  @Column(name = "title", nullable = false)
  private String title;
  @Lob
  @Column(name = "content", nullable = false)
  private byte[] content;
}