package com.example.storage;

import com.example.model.LicenseHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LicenseHistoryRepository extends JpaRepository<LicenseHistory, Long> {
}
