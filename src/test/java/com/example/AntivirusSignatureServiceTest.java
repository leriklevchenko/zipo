package com.example;

import com.example.model.*;
import com.example.service.AntivirusSignatureService;
import com.example.storage.AntivirusSignatureHistoryRepository;
import com.example.storage.AntivirusSignatureRepository;
import com.example.storage.SignatureAuditRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AntivirusSignatureServiceTest {

    @Autowired
    private AntivirusSignatureService signatureService;

    @Autowired
    private AntivirusSignatureRepository signatureRepository;

    @Autowired
    private AntivirusSignatureHistoryRepository historyRepository;

    @Autowired
    private SignatureAuditRepository auditRepository;

    @AfterEach
    void cleanup() {
        auditRepository.deleteAll();
        historyRepository.deleteAll();
        signatureRepository.deleteAll();
    }

    @Test
    void shouldSupportAllSignatureOperationsAndAuditRules() {
        Instant since = Instant.now().minusSeconds(1);

        AntivirusSignatureResponse created = signatureService.create(
                "EICAR",
                "1.0",
                "X5O!P%@AP",
                "test signature",
                "admin"
        );

        UUID signatureId = created.getId();
        assertNotNull(signatureId);
        assertEquals(AntivirusSignatureStatus.ACTIVE, created.getStatus());
        assertNotNull(created.getDigitalSignature());
        assertTrue(signatureService.verify(signatureRepository.findById(signatureId).orElseThrow()));

        AntivirusSignatureResponse loaded = signatureService.get(signatureId);
        assertEquals(signatureId, loaded.getId());

        AntivirusSignatureResponse updated = signatureService.update(
                signatureId,
                "EICAR",
                "1.1",
                "X5O!P%@AP-UPDATED",
                "updated signature",
                "admin"
        );
        assertNotEquals(created.getDigitalSignature(), updated.getDigitalSignature());
        assertTrue(signatureService.verify(signatureRepository.findById(signatureId).orElseThrow()));

        AntivirusSignatureResponse deleted = signatureService.delete(signatureId, "admin");
        assertEquals(AntivirusSignatureStatus.DELETED, deleted.getStatus());
        assertNotEquals(updated.getDigitalSignature(), deleted.getDigitalSignature());
        assertTrue(signatureRepository.findById(signatureId).isPresent());

        List<AntivirusSignatureHistoryResponse> history = signatureService.history(signatureId);
        assertEquals(2, history.size());
        assertEquals(SignatureAuditAction.UPDATE, history.get(0).getAction());
        assertEquals(SignatureAuditAction.DELETE, history.get(1).getAction());

        List<SignatureAuditResponse> audit = signatureService.audit(signatureId);
        assertEquals(3, audit.size());
        assertEquals(SignatureAuditAction.CREATE, audit.get(0).getAction());
        assertEquals(SignatureAuditAction.UPDATE, audit.get(1).getAction());
        assertEquals(SignatureAuditAction.DELETE, audit.get(2).getAction());

        assertTrue(signatureService.fullExport().stream()
                .noneMatch(signature -> signature.getStatus() == AntivirusSignatureStatus.DELETED));

        List<AntivirusSignatureResponse> incremental = signatureService.incrementalExport(since);
        assertTrue(incremental.stream().anyMatch(signature ->
                signature.getId().equals(signatureId)
                        && signature.getStatus() == AntivirusSignatureStatus.DELETED));
    }
}
