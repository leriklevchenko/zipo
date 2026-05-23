package com.example.model;

public class StoredSignatureFile {
    private final String objectKey;
    private final String originalName;
    private final String contentType;
    private final long size;
    private final String sha256;

    public StoredSignatureFile(String objectKey, String originalName, String contentType, long size, String sha256) {
        this.objectKey = objectKey;
        this.originalName = originalName;
        this.contentType = contentType;
        this.size = size;
        this.sha256 = sha256;
    }

    public String getObjectKey() { return objectKey; }
    public String getOriginalName() { return originalName; }
    public String getContentType() { return contentType; }
    public long getSize() { return size; }
    public String getSha256() { return sha256; }
}
