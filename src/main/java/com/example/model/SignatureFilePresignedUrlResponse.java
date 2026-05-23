package com.example.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class SignatureFilePresignedUrlResponse {
    private List<Item> urls;

    public List<Item> getUrls() { return urls; }
    public void setUrls(List<Item> urls) { this.urls = urls; }

    public static class Item {
        private UUID signatureId;
        private String url;
        private Instant expiresAt;
        private String originalName;
        private Long size;
        private String sha256;

        public UUID getSignatureId() { return signatureId; }
        public void setSignatureId(UUID signatureId) { this.signatureId = signatureId; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public Instant getExpiresAt() { return expiresAt; }
        public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

        public String getOriginalName() { return originalName; }
        public void setOriginalName(String originalName) { this.originalName = originalName; }

        public Long getSize() { return size; }
        public void setSize(Long size) { this.size = size; }

        public String getSha256() { return sha256; }
        public void setSha256(String sha256) { this.sha256 = sha256; }
    }
}
