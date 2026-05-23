package com.example.storage;

import com.example.model.StoredSignatureFile;

import java.time.Duration;

public interface FileStorageService {
    StoredSignatureFile uploadSignatureFile(String originalName,
                                            String contentType,
                                            byte[] content,
                                            String sha256);

    String presignedGetUrl(String objectKey, Duration expiration);
}
