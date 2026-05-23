package com.example.storage;

import com.example.model.Device;
import com.example.model.DeviceLicense;
import com.example.model.License;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceLicenseRepository extends JpaRepository<DeviceLicense, Long> {
    long countByLicense(License license);
    boolean existsByLicenseAndDevice(License license, Device device);
}
