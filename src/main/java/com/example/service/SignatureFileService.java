package com.example.service;

import com.example.model.*;
import com.example.storage.AntivirusSignatureRepository;
import com.example.storage.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class SignatureFileService {
    private final FileStorageService fileStorageService;
    private final AntivirusSignatureService signatureService;
    private final AntivirusSignatureRepository signatureRepository;
    private final Duration presignedUrlExpiration;

    public SignatureFileService(FileStorageService fileStorageService,
                                AntivirusSignatureService signatureService,
                                AntivirusSignatureRepository signatureRepository,
                                @Value("${minio.presigned-url-expiration-seconds:3600}") long presignedUrlExpirationSeconds) {
        this.fileStorageService = fileStorageService;
        this.signatureService = signatureService;
        this.signatureRepository = signatureRepository;
        this.presignedUrlExpiration = Duration.ofSeconds(presignedUrlExpirationSeconds);
    }

    public AntivirusSignatureResponse uploadAndCreateSignature(MultipartFile file,
                                                               String name,
                                                               String version,
                                                               String description,
                                                               String username) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Signature file is required");
        }

        try {
            byte[] content = file.getBytes();
            String sha256 = sha256Hex(content);
            String originalName = file.getOriginalFilename();
            String signatureName = (name == null || name.isBlank()) ? originalName : name;
            StoredSignatureFile storedFile = fileStorageService.uploadSignatureFile(
                    originalName,
                    file.getContentType(),
                    content,
                    sha256
            );
            return signatureService.createFromFile(signatureName, version, description, storedFile, username);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to upload signature file", e);
        }
    }

    public SignatureFilePresignedUrlResponse presignedUrls(List<java.util.UUID> signatureIds) {
        if (signatureIds == null || signatureIds.isEmpty()) {
            throw new RuntimeException("signatureIds is required");
        }

        Instant expiresAt = Instant.now().plus(presignedUrlExpiration);
        List<SignatureFilePresignedUrlResponse.Item> items = signatureRepository.findAllById(signatureIds)
                .stream()
                .filter(signature -> signature.getFileObjectKey() != null && !signature.getFileObjectKey().isBlank())
                .map(signature -> {
                    SignatureFilePresignedUrlResponse.Item item = new SignatureFilePresignedUrlResponse.Item();
                    item.setSignatureId(signature.getId());
                    item.setUrl(fileStorageService.presignedGetUrl(signature.getFileObjectKey(), presignedUrlExpiration));
                    item.setExpiresAt(expiresAt);
                    item.setOriginalName(signature.getFileOriginalName());
                    item.setSize(signature.getFileSize());
                    item.setSha256(signature.getFileSha256());
                    return item;
                })
                .toList();

        SignatureFilePresignedUrlResponse response = new SignatureFilePresignedUrlResponse();
        response.setUrls(items);
        return response;
    }

    private String sha256Hex(byte[] payload) throws Exception {
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(payload);
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte value : hash) {
            hex.append(String.format("%02x", value));
        }
        return hex.toString();
    }
}
