package com.example.storage;

import com.example.model.AntivirusSignatureHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AntivirusSignatureHistoryRepository extends JpaRepository<AntivirusSignatureHistory, UUID> {
    List<AntivirusSignatureHistory> findBySignature_IdOrderByChangedAtAsc(UUID signatureId);
}
