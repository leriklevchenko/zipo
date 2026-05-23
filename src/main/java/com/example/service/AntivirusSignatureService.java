package com.example.service;

import com.example.model.*;
import com.example.signature.DigitalSignatureService;
import com.example.storage.AntivirusSignatureHistoryRepository;
import com.example.storage.AntivirusSignatureRepository;
import com.example.storage.SignatureAuditRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AntivirusSignatureService {

    private final AntivirusSignatureRepository signatureRepository;
    private final AntivirusSignatureHistoryRepository historyRepository;
    private final SignatureAuditRepository auditRepository;
    private final DigitalSignatureService digitalSignatureService;

    public AntivirusSignatureService(AntivirusSignatureRepository signatureRepository,
                                     AntivirusSignatureHistoryRepository historyRepository,
                                     SignatureAuditRepository auditRepository,
                                     DigitalSignatureService digitalSignatureService) {
        this.signatureRepository = signatureRepository;
        this.historyRepository = historyRepository;
        this.auditRepository = auditRepository;
        this.digitalSignatureService = digitalSignatureService;
    }

    public AntivirusSignatureResponse create(String name, String version, String pattern, String description, String username) {
        requireText(name, "Signature name is required");
        requireText(version, "Signature version is required");
        requireText(pattern, "Signature pattern is required");

        Instant now = Instant.now();
        AntivirusSignature signature = new AntivirusSignature();
        signature.setName(name);
        signature.setVersion(version);
        signature.setPattern(pattern);
        signature.setDescription(description);
        signature.setStatus(AntivirusSignatureStatus.ACTIVE);
        signature.setCreatedAt(now);
        signature.setUpdatedAt(now);
        signature.setDigitalSignature("pending");

        AntivirusSignature saved = signatureRepository.save(signature);
        saved.setDigitalSignature(sign(saved));
        saved = signatureRepository.save(saved);
        writeAudit(saved.getId(), SignatureAuditAction.CREATE, username, now);
        return mapSignature(saved);
    }

    public AntivirusSignatureResponse get(UUID id) {
        return mapSignature(findSignature(id));
    }

    public AntivirusSignatureResponse update(UUID id, String name, String version, String pattern, String description, String username) {
        AntivirusSignature signature = findSignature(id);
        if (signature.getStatus() == AntivirusSignatureStatus.DELETED) {
            throw new RuntimeException("Deleted signature cannot be updated");
        }

        writeHistory(signature, SignatureAuditAction.UPDATE);

        if (name != null) {
            requireText(name, "Signature name is required");
            signature.setName(name);
        }
        if (version != null) {
            requireText(version, "Signature version is required");
            signature.setVersion(version);
        }
        if (pattern != null) {
            requireText(pattern, "Signature pattern is required");
            signature.setPattern(pattern);
        }
        if (description != null) {
            signature.setDescription(description);
        }

        Instant now = Instant.now();
        signature.setUpdatedAt(now);
        signature.setDigitalSignature(sign(signature));

        AntivirusSignature saved = signatureRepository.save(signature);
        writeAudit(saved.getId(), SignatureAuditAction.UPDATE, username, now);
        return mapSignature(saved);
    }

    public AntivirusSignatureResponse delete(UUID id, String username) {
        AntivirusSignature signature = findSignature(id);
        if (signature.getStatus() != AntivirusSignatureStatus.DELETED) {
            writeHistory(signature, SignatureAuditAction.DELETE);
            Instant now = Instant.now();
            signature.setStatus(AntivirusSignatureStatus.DELETED);
            signature.setUpdatedAt(now);
            signature.setDigitalSignature(sign(signature));
            signatureRepository.save(signature);
            writeAudit(signature.getId(), SignatureAuditAction.DELETE, username, now);
        }
        return mapSignature(signature);
    }

    public List<AntivirusSignatureResponse> fullExport() {
        return signatureRepository.findByStatusNotOrderByUpdatedAtAsc(AntivirusSignatureStatus.DELETED)
                .stream()
                .map(this::mapSignature)
                .toList();
    }

    public List<AntivirusSignatureResponse> incrementalExport(Instant since) {
        return signatureRepository.findByUpdatedAtAfterOrderByUpdatedAtAsc(since)
                .stream()
                .map(this::mapSignature)
                .toList();
    }

    public List<AntivirusSignatureHistoryResponse> history(UUID signatureId) {
        findSignature(signatureId);
        return historyRepository.findBySignature_IdOrderByChangedAtAsc(signatureId)
                .stream()
                .map(this::mapHistory)
                .toList();
    }

    public List<SignatureAuditResponse> audit(UUID signatureId) {
        findSignature(signatureId);
        return auditRepository.findBySignatureIdOrderByCreatedAtAsc(signatureId)
                .stream()
                .map(this::mapAudit)
                .toList();
    }

    public boolean verify(AntivirusSignature signature) {
        return digitalSignatureService.verifyData(canonicalPayload(signature), signature.getDigitalSignature());
    }

    private AntivirusSignature findSignature(UUID id) {
        return signatureRepository.findById(id).orElseThrow(() -> new RuntimeException("Signature not found"));
    }

    private void writeHistory(AntivirusSignature signature, SignatureAuditAction action) {
        AntivirusSignatureHistory history = new AntivirusSignatureHistory();
        history.setSignature(signature);
        history.setAction(action);
        history.setName(signature.getName());
        history.setVersion(signature.getVersion());
        history.setPattern(signature.getPattern());
        history.setDescription(signature.getDescription());
        history.setStatus(signature.getStatus());
        history.setDigitalSignature(signature.getDigitalSignature());
        history.setChangedAt(Instant.now());
        historyRepository.save(history);
    }

    private void writeAudit(UUID signatureId, SignatureAuditAction action, String username, Instant createdAt) {
        SignatureAudit audit = new SignatureAudit();
        audit.setSignatureId(signatureId);
        audit.setAction(action);
        audit.setUsername((username == null || username.isBlank()) ? "system" : username);
        audit.setCreatedAt(createdAt);
        auditRepository.save(audit);
    }

    private String sign(AntivirusSignature signature) {
        return digitalSignatureService.signData(canonicalPayload(signature));
    }

    private byte[] canonicalPayload(AntivirusSignature signature) {
        String payload = "{"
                + "\"id\":\"" + value(signature.getId()) + "\","
                + "\"name\":\"" + escapeJson(signature.getName()) + "\","
                + "\"version\":\"" + escapeJson(signature.getVersion()) + "\","
                + "\"pattern\":\"" + escapeJson(signature.getPattern()) + "\","
                + "\"description\":\"" + escapeJson(signature.getDescription()) + "\","
                + "\"status\":\"" + signature.getStatus() + "\","
                + "\"createdAt\":" + toEpochMillis(signature.getCreatedAt()) + ","
                + "\"updatedAt\":" + toEpochMillis(signature.getUpdatedAt())
                + "}";
        return payload.getBytes(StandardCharsets.UTF_8);
    }

    private AntivirusSignatureResponse mapSignature(AntivirusSignature signature) {
        AntivirusSignatureResponse response = new AntivirusSignatureResponse();
        response.setId(signature.getId());
        response.setName(signature.getName());
        response.setVersion(signature.getVersion());
        response.setPattern(signature.getPattern());
        response.setDescription(signature.getDescription());
        response.setStatus(signature.getStatus());
        response.setDigitalSignature(signature.getDigitalSignature());
        response.setCreatedAt(signature.getCreatedAt());
        response.setUpdatedAt(signature.getUpdatedAt());
        return response;
    }

    private AntivirusSignatureHistoryResponse mapHistory(AntivirusSignatureHistory history) {
        AntivirusSignatureHistoryResponse response = new AntivirusSignatureHistoryResponse();
        response.setId(history.getId());
        response.setSignatureId(history.getSignature().getId());
        response.setAction(history.getAction());
        response.setName(history.getName());
        response.setVersion(history.getVersion());
        response.setPattern(history.getPattern());
        response.setDescription(history.getDescription());
        response.setStatus(history.getStatus());
        response.setDigitalSignature(history.getDigitalSignature());
        response.setChangedAt(history.getChangedAt());
        return response;
    }

    private SignatureAuditResponse mapAudit(SignatureAudit audit) {
        SignatureAuditResponse response = new SignatureAuditResponse();
        response.setId(audit.getId());
        response.setSignatureId(audit.getSignatureId());
        response.setAction(audit.getAction());
        response.setUsername(audit.getUsername());
        response.setCreatedAt(audit.getCreatedAt());
        return response;
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new RuntimeException(message);
        }
    }

    private String value(Object value) {
        return value != null ? value.toString() : "";
    }

    private String toEpochMillis(Instant value) {
        return value != null ? String.valueOf(value.toEpochMilli()) : "null";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
