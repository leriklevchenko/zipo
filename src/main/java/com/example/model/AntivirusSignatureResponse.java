package com.example.model;

import java.time.Instant;
import java.util.UUID;

public class AntivirusSignatureResponse {
    private UUID id;
    private String name;
    private String version;
    private String pattern;
    private String description;
    private AntivirusSignatureStatus status;
    private String digitalSignature;
    private Instant createdAt;
    private Instant updatedAt;
    private String fileObjectKey;
    private String fileOriginalName;
    private String fileContentType;
    private Long fileSize;
    private String fileSha256;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

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

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getFileObjectKey() { return fileObjectKey; }
    public void setFileObjectKey(String fileObjectKey) { this.fileObjectKey = fileObjectKey; }

    public String getFileOriginalName() { return fileOriginalName; }
    public void setFileOriginalName(String fileOriginalName) { this.fileOriginalName = fileOriginalName; }

    public String getFileContentType() { return fileContentType; }
    public void setFileContentType(String fileContentType) { this.fileContentType = fileContentType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getFileSha256() { return fileSha256; }
    public void setFileSha256(String fileSha256) { this.fileSha256 = fileSha256; }
}
