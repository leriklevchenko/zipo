package com.example.storage;

import com.example.model.License;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LicenseRepository extends JpaRepository<License, UUID> {
    Optional<License> findByUser_IdAndDeviceId(Long userId, String deviceId);
}
