package com.example.storage;

import com.example.model.SignatureAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SignatureAuditRepository extends JpaRepository<SignatureAudit, UUID> {
    List<SignatureAudit> findBySignatureIdOrderByCreatedAtAsc(UUID signatureId);
}
