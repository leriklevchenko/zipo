package com.example.model;

import java.time.Instant;
import java.util.UUID;

public class LicenseResponse {

    private UUID id;
    private String code;
    private Long productId;
    private Long typeId;
    private Long ownerId;
    private Long userId;
    private Instant firstActivationDate;
    private Instant endingDate;
    private int deviceCount;
    private String description;
    private LicenseStatus status;
    private boolean blocked;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Long getTypeId() { return typeId; }
    public void setTypeId(Long typeId) { this.typeId = typeId; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Instant getFirstActivationDate() { return firstActivationDate; }
    public void setFirstActivationDate(Instant firstActivationDate) { this.firstActivationDate = firstActivationDate; }

    public Instant getEndingDate() { return endingDate; }
    public void setEndingDate(Instant endingDate) { this.endingDate = endingDate; }

    public int getDeviceCount() { return deviceCount; }
    public void setDeviceCount(int deviceCount) { this.deviceCount = deviceCount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LicenseStatus getStatus() { return status; }
    public void setStatus(LicenseStatus status) { this.status = status; }

    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
}
