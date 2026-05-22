package com.example.controller;

import com.example.model.License;
import com.example.model.LicenseResponse;
import com.example.model.TicketResponse;
import com.example.service.LicenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user/license")
public class LicenseController {

    private final LicenseService licenseService;

    public LicenseController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @PostMapping("/create")
    public ResponseEntity<LicenseResponse> create(@RequestBody CreateLicenseRequest request) {
        License license = licenseService.createLicense(request.getUserId(), request.getDeviceId(), request.getValidityDays());
        return ResponseEntity.ok(mapToResponse(license));
    }

    private LicenseResponse mapToResponse(License license) {
        LicenseResponse response = new LicenseResponse();
        response.setId(license.getId());
        response.setUserId(license.getUser().getId());
        response.setDeviceId(license.getDeviceId());
        response.setCreatedAt(license.getCreatedAt());
        response.setActivatedAt(license.getActivatedAt());
        response.setExpiresAt(license.getExpiresAt());
        response.setValidityDays(license.getValidityDays());
        response.setStatus(license.getStatus());
        response.setBlocked(license.isBlocked());
        return response;
    }

    @PostMapping("/activate")
    public ResponseEntity<TicketResponse> activate(@RequestBody ActivateLicenseRequest request) {
        TicketResponse response = licenseService.activateLicense(request.getLicenseId(), request.getTicketLifetimeSeconds());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check")
    public ResponseEntity<TicketResponse> check(@RequestParam UUID licenseId,
                                                @RequestParam(defaultValue = "900") int ticketLifetimeSeconds) {
        TicketResponse response = licenseService.checkLicense(licenseId, ticketLifetimeSeconds);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/extend")
    public ResponseEntity<TicketResponse> extend(@RequestBody ExtendLicenseRequest request) {
        TicketResponse response = licenseService.extendLicense(request.getLicenseId(), request.getAdditionalDays(), request.getTicketLifetimeSeconds());
        return ResponseEntity.ok(response);
    }

    public static class CreateLicenseRequest {
        private Long userId;
        private String deviceId;
        private int validityDays;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

        public int getValidityDays() { return validityDays; }
        public void setValidityDays(int validityDays) { this.validityDays = validityDays; }
    }

    public static class ActivateLicenseRequest {
        private UUID licenseId;
        private int ticketLifetimeSeconds = 900;

        public UUID getLicenseId() { return licenseId; }
        public void setLicenseId(UUID licenseId) { this.licenseId = licenseId; }

        public int getTicketLifetimeSeconds() { return ticketLifetimeSeconds; }
        public void setTicketLifetimeSeconds(int ticketLifetimeSeconds) { this.ticketLifetimeSeconds = ticketLifetimeSeconds; }
    }

    public static class ExtendLicenseRequest {
        private UUID licenseId;
        private int additionalDays;
        private int ticketLifetimeSeconds = 900;

        public UUID getLicenseId() { return licenseId; }
        public void setLicenseId(UUID licenseId) { this.licenseId = licenseId; }

        public int getAdditionalDays() { return additionalDays; }
        public void setAdditionalDays(int additionalDays) { this.additionalDays = additionalDays; }

        public int getTicketLifetimeSeconds() { return ticketLifetimeSeconds; }
        public void setTicketLifetimeSeconds(int ticketLifetimeSeconds) { this.ticketLifetimeSeconds = ticketLifetimeSeconds; }
    }
}
