package com.example.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "antivirus_signature_history")
public class AntivirusSignatureHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signature_id", nullable = false)
    private AntivirusSignature signature;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SignatureAuditAction action;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String version;

    @Column(nullable = false, length = 4000)
    private String pattern;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AntivirusSignatureStatus status;

    @Column(nullable = false, length = 1000)
    private String digitalSignature;

    @Column(nullable = false)
    private Instant changedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public AntivirusSignature getSignature() { return signature; }
    public void setSignature(AntivirusSignature signature) { this.signature = signature; }

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
