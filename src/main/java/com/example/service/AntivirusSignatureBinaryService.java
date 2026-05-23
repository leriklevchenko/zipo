package com.example.service;

import com.example.model.*;
import com.example.signature.DigitalSignatureService;
import com.example.storage.AntivirusSignatureRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.List;

@Service
public class AntivirusSignatureBinaryService {
    private final AntivirusSignatureRepository signatureRepository;
    private final DigitalSignatureService digitalSignatureService;

    public AntivirusSignatureBinaryService(AntivirusSignatureRepository signatureRepository,
                                           DigitalSignatureService digitalSignatureService) {
        this.signatureRepository = signatureRepository;
        this.digitalSignatureService = digitalSignatureService;
    }

    public SignatureBinaryPackage fullExport() {
        List<AntivirusSignature> signatures = signatureRepository
                .findByStatusNotOrderByUpdatedAtAsc(AntivirusSignatureStatus.DELETED);
        return buildPackage(SignatureBinaryExportType.FULL, null, signatures);
    }

    public SignatureBinaryPackage incrementalExport(Instant since) {
        List<AntivirusSignature> signatures = signatureRepository.findByUpdatedAtAfterOrderByUpdatedAtAsc(since);
        return buildPackage(SignatureBinaryExportType.INCREMENTAL, since, signatures);
    }

    private SignatureBinaryPackage buildPackage(SignatureBinaryExportType exportType,
                                                Instant since,
                                                List<AntivirusSignature> signatures) {
        byte[] data = SignatureBinaryProtocol.writeData(exportType, signatures);

        SignatureBinaryManifest manifest = new SignatureBinaryManifest();
        manifest.setExportType(exportType);
        manifest.setGeneratedAt(Instant.now());
        manifest.setSince(since);
        manifest.setRecordCount(signatures.size());
        manifest.setDataLength(data.length);
        manifest.setDataSha256(sha256(data));

        byte[] manifestBytes = SignatureBinaryProtocol.writeManifest(manifest);
        byte[] manifestSignature = digitalSignatureService.signManifest(manifestBytes).getBytes(StandardCharsets.US_ASCII);
        return new SignatureBinaryPackage(manifestBytes, manifestSignature, data, signatures.size());
    }

    private byte[] sha256(byte[] payload) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(payload);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash signature binary data", e);
        }
    }
}
