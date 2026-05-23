package com.example.controller;

import com.example.model.License;
import com.example.model.LicenseResponse;
import com.example.model.TicketResponse;
import com.example.service.LicenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api")
public class LicenseController {

    private final LicenseService licenseService;

    public LicenseController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @PostMapping("/admin/licenses")
    public ResponseEntity<LicenseResponse> create(@RequestBody CreateLicenseRequest request,
                                                  Authentication authentication) {
        License license = licenseService.createLicense(
                request.getProductId(),
                request.getTypeId(),
                request.getOwnerId(),
                request.getDeviceCount(),
                request.getDescription(),
                authentication.getName()
        );
        return ResponseEntity.created(URI.create("/api/admin/licenses/" + license.getId()))
                .body(mapToResponse(license));
    }

    @PostMapping("/user/licenses/activate")
    public ResponseEntity<TicketResponse> activate(@RequestBody ActivateLicenseRequest request,
                                                   Authentication authentication) {
        TicketResponse response = licenseService.activateLicense(
                request.getActivationKey(),
                request.getDeviceMac(),
                request.getDeviceName(),
                request.getTicketLifetimeSeconds(),
                authentication.getName()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/licenses/renew")
    public ResponseEntity<TicketResponse> renew(@RequestBody RenewLicenseRequest request,
                                                Authentication authentication) {
        TicketResponse response = licenseService.renewLicense(
                request.getActivationKey(),
                request.getTicketLifetimeSeconds(),
                authentication.getName()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/licenses/check")
    public ResponseEntity<TicketResponse> check(@RequestParam String deviceMac,
                                                @RequestParam Long productId,
                                                @RequestParam(defaultValue = "900") int ticketLifetimeSeconds,
                                                Authentication authentication) {
        TicketResponse response = licenseService.checkLicense(deviceMac, productId, ticketLifetimeSeconds, authentication.getName());
        return ResponseEntity.ok(response);
    }

    private LicenseResponse mapToResponse(License license) {
        LicenseResponse response = new LicenseResponse();
        response.setId(license.getId());
        response.setCode(license.getCode());
        response.setProductId(license.getProduct().getId());
        response.setTypeId(license.getType().getId());
        response.setOwnerId(license.getOwner().getId());
        response.setUserId(license.getUser() == null ? null : license.getUser().getId());
        response.setFirstActivationDate(license.getFirstActivationDate());
        response.setEndingDate(license.getEndingDate());
        response.setDeviceCount(license.getDeviceCount());
        response.setDescription(license.getDescription());
        response.setStatus(license.getStatus());
        response.setBlocked(license.isBlocked());
        return response;
    }

    public static class CreateLicenseRequest {
        private Long productId;
        private Long typeId;
        private Long ownerId;
        private int deviceCount = 1;
        private String description;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public Long getTypeId() { return typeId; }
        public void setTypeId(Long typeId) { this.typeId = typeId; }

        public Long getOwnerId() { return ownerId; }
        public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

        public int getDeviceCount() { return deviceCount; }
        public void setDeviceCount(int deviceCount) { this.deviceCount = deviceCount; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class ActivateLicenseRequest {
        private String activationKey;
        private String deviceMac;
        private String deviceName;
        private int ticketLifetimeSeconds = 900;

        public String getActivationKey() { return activationKey; }
        public void setActivationKey(String activationKey) { this.activationKey = activationKey; }

        public String getDeviceMac() { return deviceMac; }
        public void setDeviceMac(String deviceMac) { this.deviceMac = deviceMac; }

        public String getDeviceName() { return deviceName; }
        public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

        public int getTicketLifetimeSeconds() { return ticketLifetimeSeconds; }
        public void setTicketLifetimeSeconds(int ticketLifetimeSeconds) { this.ticketLifetimeSeconds = ticketLifetimeSeconds; }
    }

    public static class RenewLicenseRequest {
        private String activationKey;
        private int ticketLifetimeSeconds = 900;

        public String getActivationKey() { return activationKey; }
        public void setActivationKey(String activationKey) { this.activationKey = activationKey; }

        public int getTicketLifetimeSeconds() { return ticketLifetimeSeconds; }
        public void setTicketLifetimeSeconds(int ticketLifetimeSeconds) { this.ticketLifetimeSeconds = ticketLifetimeSeconds; }
    }
}
