package com.example.storage;

import com.example.model.AntivirusSignature;
import com.example.model.AntivirusSignatureStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AntivirusSignatureRepository extends JpaRepository<AntivirusSignature, UUID> {
    List<AntivirusSignature> findByStatusNotOrderByUpdatedAtAsc(AntivirusSignatureStatus status);
    List<AntivirusSignature> findByUpdatedAtAfterOrderByUpdatedAtAsc(Instant since);
}
