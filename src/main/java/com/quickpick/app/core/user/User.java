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
@Table(name = "users")
@Getter
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(staticName = "create")
public final class User {
  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;
  @Column(name = "phone_number", unique = true, nullable = false)
  private String phoneNumber;
  @Column(name = "name", nullable = false)
  private String name;
  @Column(name = "compliant", nullable = false)
  private boolean compliant;
  @Column(name = "public_key", nullable = false, columnDefinition = "TEXT")
  private String publicKey;
  @Column(name = "firebase_token", nullable = false, columnDefinition = "TEXT")
  private String firebaseToken;
  @Column(name = "joined_at", nullable = false, updatable = false)
  private long joinedAt;

  public void changeName(String newName) {
    this.name = newName;
  }

  public void changePublicKey(String publicKey) {
    this.publicKey = publicKey;
  }

  public void changeFirebaseToken(String firebaseToken) {
    this.firebaseToken = firebaseToken;
  }
}
