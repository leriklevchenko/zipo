package com.example.storage;

import com.example.model.StoredSignatureFile;
import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class MinioFileStorageService implements FileStorageService {
    private final MinioClient minioClient;
    private final String bucket;
    private final String publicEndpoint;

    public MinioFileStorageService(@Value("${minio.endpoint}") String endpoint,
                                   @Value("${minio.public-endpoint:${minio.endpoint}}") String publicEndpoint,
                                   @Value("${minio.access-key}") String accessKey,
                                   @Value("${minio.secret-key}") String secretKey,
                                   @Value("${minio.bucket}") String bucket) {
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        this.publicEndpoint = publicEndpoint;
        this.bucket = bucket;
    }

    @Override
    public StoredSignatureFile uploadSignatureFile(String originalName,
                                                  String contentType,
                                                  byte[] content,
                                                  String sha256) {
        ensureBucket();
        String objectKey = "signatures/" + UUID.randomUUID() + "/" + sanitize(originalName);
        String normalizedContentType = contentType == null || contentType.isBlank()
                ? "application/octet-stream"
                : contentType;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content)) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(inputStream, content.length, -1)
                    .contentType(normalizedContentType)
                    .headers(Map.of("X-Amz-Meta-Sha256", sha256))
                    .build());
            return new StoredSignatureFile(objectKey, originalName, normalizedContentType, content.length, sha256);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to upload signature file to MinIO", e);
        }
    }

    @Override
    public String presignedGetUrl(String objectKey, Duration expiration) {
        try {
            String internalUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object(objectKey)
                    .expiry(Math.toIntExact(expiration.toSeconds()), TimeUnit.SECONDS)
                    .build());
            return rewritePublicEndpoint(internalUrl);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create MinIO pre-signed URL", e);
        }
    }

    private void ensureBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to prepare private MinIO bucket", e);
        }
    }

    private String rewritePublicEndpoint(String internalUrl) {
        if (publicEndpoint == null || publicEndpoint.isBlank()) {
            return internalUrl;
        }
        try {
            URI source = URI.create(internalUrl);
            URI publicUri = URI.create(publicEndpoint);
            return new URI(
                    publicUri.getScheme(),
                    publicUri.getUserInfo(),
                    publicUri.getHost(),
                    publicUri.getPort(),
                    source.getPath(),
                    source.getQuery(),
                    source.getFragment()
            ).toString();
        } catch (Exception e) {
            return internalUrl;
        }
    }

    private String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "signature.bin";
        }
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
