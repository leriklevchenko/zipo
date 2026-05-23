package com.example.model;

import java.time.Instant;
import java.util.UUID;

public class AntivirusSignatureHistoryResponse {
    private UUID id;
    private UUID signatureId;
    private SignatureAuditAction action;
    private String name;
    private String version;
    private String pattern;
    private String description;
    private AntivirusSignatureStatus status;
    private String digitalSignature;
    private Instant changedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getSignatureId() { return signatureId; }
    public void setSignatureId(UUID signatureId) { this.signatureId = signatureId; }

    public SignatureAuditAction getAction() { return action; }
    public void setAction(SignatureAuditAction action) { this.action = action; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getPattern() { return pattern; }
    public void setPattern(String pattern) { this.pattern = pattern; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public AntivirusSignatureStatus getStatus() { return status; }
    public void setStatus(AntivirusSignatureStatus status) { this.status = status; }

    public String getDigitalSignature() { return digitalSignature; }
    public void setDigitalSignature(String digitalSignature) { this.digitalSignature = digitalSignature; }

    public Instant getChangedAt() { return changedAt; }
    public void setChangedAt(Instant changedAt) { this.changedAt = changedAt; }
}
