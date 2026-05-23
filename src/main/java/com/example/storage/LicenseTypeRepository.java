package com.example.storage;

import com.example.model.LicenseType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LicenseTypeRepository extends JpaRepository<LicenseType, Long> {
}
