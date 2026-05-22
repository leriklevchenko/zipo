package com.example.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "licenses")
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant activatedAt;
    private Instant expiresAt;

    @Column(nullable = false)
    private int validityDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LicenseStatus status;

    @Column(nullable = false)
    private boolean blocked;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getActivatedAt() { return activatedAt; }
    public void setActivatedAt(Instant activatedAt) { this.activatedAt = activatedAt; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public int getValidityDays() { return validityDays; }
    public void setValidityDays(int validityDays) { this.validityDays = validityDays; }

    public LicenseStatus getStatus() { return status; }
    public void setStatus(LicenseStatus status) { this.status = status; }

    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
}
