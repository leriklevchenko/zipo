package com.example.model;

import java.time.Instant;
import java.util.UUID;

public class SignatureAuditResponse {
    private UUID id;
    private UUID signatureId;
    private SignatureAuditAction action;
    private String username;
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getSignatureId() { return signatureId; }
    public void setSignatureId(UUID signatureId) { this.signatureId = signatureId; }

    public SignatureAuditAction getAction() { return action; }
    public void setAction(SignatureAuditAction action) { this.action = action; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
