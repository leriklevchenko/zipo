package com.example.model;

import java.time.Instant;

public class Ticket {

    private Instant serverDate;
    private long ticketLifetimeSeconds;
    private Instant activationDate;
    private Instant expirationDate;
    private String licenseCode;
    private Long productId;
    private Long userId;
    private String deviceId;
    private boolean blocked;

    public Instant getServerDate() { return serverDate; }
    public void setServerDate(Instant serverDate) { this.serverDate = serverDate; }

    public long getTicketLifetimeSeconds() { return ticketLifetimeSeconds; }
    public void setTicketLifetimeSeconds(long ticketLifetimeSeconds) { this.ticketLifetimeSeconds = ticketLifetimeSeconds; }

    public Instant getActivationDate() { return activationDate; }
    public void setActivationDate(Instant activationDate) { this.activationDate = activationDate; }

    public Instant getExpirationDate() { return expirationDate; }
    public void setExpirationDate(Instant expirationDate) { this.expirationDate = expirationDate; }

    public String getLicenseCode() { return licenseCode; }
    public void setLicenseCode(String licenseCode) { this.licenseCode = licenseCode; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
}
