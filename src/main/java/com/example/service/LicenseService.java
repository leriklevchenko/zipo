package com.example.service;

import com.example.model.License;
import com.example.model.LicenseStatus;
import com.example.model.Ticket;
import com.example.model.TicketResponse;
import com.example.model.User;
import com.example.signature.DigitalSignatureService;
import com.example.storage.LicenseRepository;
import com.example.storage.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class LicenseService {

    private final LicenseRepository licenseRepository;
    private final UserRepository userRepository;
    private final DigitalSignatureService digitalSignatureService;

    public LicenseService(LicenseRepository licenseRepository,
                          UserRepository userRepository,
                          DigitalSignatureService digitalSignatureService) {
        this.licenseRepository = licenseRepository;
        this.userRepository = userRepository;
        this.digitalSignatureService = digitalSignatureService;
    }

    public License createLicense(Long userId, String deviceId, int validityDays) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new RuntimeException("User not found")
        );
        if (deviceId == null || deviceId.isBlank()) {
            throw new RuntimeException("Device ID is required");
        }
        if (validityDays <= 0) {
            throw new RuntimeException("Validity days must be greater than zero");
        }

        License license = new License();
        license.setUser(user);
        license.setDeviceId(deviceId);
        license.setCreatedAt(Instant.now());
        license.setValidityDays(validityDays);
        license.setStatus(LicenseStatus.CREATED);
        license.setBlocked(false);
        return licenseRepository.save(license);
    }

    public TicketResponse activateLicense(UUID licenseId, int ticketLifetimeSeconds) {
        License license = licenseRepository.findById(licenseId).orElseThrow(
                () -> new RuntimeException("License not found")
        );
        if (license.isBlocked()) {
            throw new RuntimeException("License is blocked");
        }
        if (license.getStatus() == LicenseStatus.ACTIVE) {
            throw new RuntimeException("License already active");
        }

        Instant now = Instant.now();
        license.setActivatedAt(now);
        license.setExpiresAt(now.plus(license.getValidityDays(), ChronoUnit.DAYS));
        license.setStatus(LicenseStatus.ACTIVE);
        licenseRepository.save(license);

        return buildTicketResponse(license, ticketLifetimeSeconds);
    }

    public TicketResponse checkLicense(UUID licenseId, int ticketLifetimeSeconds) {
        License license = licenseRepository.findById(licenseId).orElseThrow(
                () -> new RuntimeException("License not found")
        );

        if (license.isBlocked()) {
            license.setStatus(LicenseStatus.BLOCKED);
            licenseRepository.save(license);
        } else if (license.getExpiresAt() != null && license.getExpiresAt().isBefore(Instant.now())) {
            license.setStatus(LicenseStatus.EXPIRED);
            licenseRepository.save(license);
        }

        return buildTicketResponse(license, ticketLifetimeSeconds);
    }

    public TicketResponse extendLicense(UUID licenseId, int additionalDays, int ticketLifetimeSeconds) {
        License license = licenseRepository.findById(licenseId).orElseThrow(
                () -> new RuntimeException("License not found")
        );
        if (license.isBlocked()) {
            throw new RuntimeException("License is blocked");
        }
        if (license.getActivatedAt() == null) {
            throw new RuntimeException("License must be activated before extension");
        }
        if (additionalDays <= 0) {
            throw new RuntimeException("Extension days must be greater than zero");
        }

        Instant now = Instant.now();
        Instant expiresAt = license.getExpiresAt();
        if (expiresAt == null || expiresAt.isBefore(now)) {
            license.setExpiresAt(now.plus(additionalDays, ChronoUnit.DAYS));
        } else {
            license.setExpiresAt(expiresAt.plus(additionalDays, ChronoUnit.DAYS));
        }
        license.setStatus(LicenseStatus.ACTIVE);
        licenseRepository.save(license);

        return buildTicketResponse(license, ticketLifetimeSeconds);
    }

    private TicketResponse buildTicketResponse(License license, int ticketLifetimeSeconds) {
        Ticket ticket = new Ticket();
        ticket.setServerDate(Instant.now());
        ticket.setTicketLifetimeSeconds(ticketLifetimeSeconds);
        ticket.setActivationDate(license.getActivatedAt());
        ticket.setExpirationDate(license.getExpiresAt());
        ticket.setUserId(license.getUser().getId());
        ticket.setDeviceId(license.getDeviceId());
        ticket.setBlocked(license.isBlocked());

        String signature = digitalSignatureService.signTicket(ticket);
        return new TicketResponse(ticket, signature);
    }
}
