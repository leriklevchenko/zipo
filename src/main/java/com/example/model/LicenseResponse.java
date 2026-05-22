package com.example.model;

import java.time.Instant;
import java.util.UUID;

public class LicenseResponse {

    private UUID id;
    private Long userId;
    private String deviceId;
    private Instant createdAt;
    private Instant activatedAt;
    private Instant expiresAt;
    private int validityDays;
    private LicenseStatus status;
    private boolean blocked;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

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
