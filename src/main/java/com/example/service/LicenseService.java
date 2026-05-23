package com.example.service;

import com.example.model.*;
import com.example.signature.DigitalSignatureService;
import com.example.storage.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class LicenseService {

    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final LicenseRepository licenseRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final LicenseTypeRepository licenseTypeRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceLicenseRepository deviceLicenseRepository;
    private final LicenseHistoryRepository licenseHistoryRepository;
    private final DigitalSignatureService digitalSignatureService;

    public LicenseService(LicenseRepository licenseRepository,
                          UserRepository userRepository,
                          ProductRepository productRepository,
                          LicenseTypeRepository licenseTypeRepository,
                          DeviceRepository deviceRepository,
                          DeviceLicenseRepository deviceLicenseRepository,
                          LicenseHistoryRepository licenseHistoryRepository,
                          DigitalSignatureService digitalSignatureService) {
        this.licenseRepository = licenseRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.licenseTypeRepository = licenseTypeRepository;
        this.deviceRepository = deviceRepository;
        this.deviceLicenseRepository = deviceLicenseRepository;
        this.licenseHistoryRepository = licenseHistoryRepository;
        this.digitalSignatureService = digitalSignatureService;
    }

    @Transactional
    public License createLicense(Long productId, Long typeId, Long ownerId, int deviceCount,
                                 String description, String adminUsername) {
        User admin = getUserByUsername(adminUsername);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> notFound("Product not found"));
        if (product.isBlocked()) {
            throw conflict("Product is blocked");
        }
        LicenseType type = licenseTypeRepository.findById(typeId)
                .orElseThrow(() -> notFound("License type not found"));
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> notFound("Owner not found"));
        if (deviceCount <= 0) {
            throw conflict("Device count must be greater than zero");
        }

        License license = new License();
        license.setCode(generateUniqueCode());
        license.setProduct(product);
        license.setType(type);
        license.setOwner(owner);
        license.setDeviceCount(deviceCount);
        license.setDescription(description);
        license.setStatus(LicenseStatus.CREATED);
        license.setBlocked(false);

        License saved = licenseRepository.save(license);
        saveHistory(saved, admin, "CREATED", "License created");
        return saved;
    }

    @Transactional
    public TicketResponse activateLicense(String activationKey, String deviceMac, String deviceName,
                                          int ticketLifetimeSeconds, String username) {
        User currentUser = getUserByUsername(username);
        License license = getLicenseByCode(activationKey);
        ensureNotBlocked(license);

        if (license.getUser() != null && !license.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "License owned by another user");
        }

        Device device = findOrCreateDevice(currentUser, deviceMac, deviceName);
        boolean alreadyLinked = deviceLicenseRepository.existsByLicenseAndDevice(license, device);
        Instant now = Instant.now();

        if (license.getUser() == null) {
            license.setUser(currentUser);
            license.setFirstActivationDate(now);
            license.setEndingDate(now.plus(license.getType().getDefaultDurationInDays(), ChronoUnit.DAYS));
            license.setStatus(LicenseStatus.ACTIVE);
            licenseRepository.save(license);
        }

        if (!alreadyLinked) {
            long activatedDevices = deviceLicenseRepository.countByLicense(license);
            if (activatedDevices >= license.getDeviceCount()) {
                throw conflict("Device limit reached");
            }
            DeviceLicense deviceLicense = new DeviceLicense();
            deviceLicense.setLicense(license);
            deviceLicense.setDevice(device);
            deviceLicense.setActivationDate(now);
            deviceLicenseRepository.save(deviceLicense);
        }

        saveHistory(license, currentUser, "ACTIVATED", "License activated for device " + device.getMacAddress());
        return buildTicketResponse(license, device, ticketLifetimeSeconds);
    }

    @Transactional
    public TicketResponse renewLicense(String activationKey, int ticketLifetimeSeconds, String username) {
        User currentUser = getUserByUsername(username);
        License license = getLicenseByCode(activationKey);
        ensureNotBlocked(license);

        if (license.getUser() != null && !license.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "License owned by another user");
        }
        if (license.getUser() == null && !license.getOwner().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can renew inactive license");
        }

        Instant now = Instant.now();
        Instant endingDate = license.getEndingDate();
        boolean renewAllowed = endingDate == null || !endingDate.isAfter(now.plus(7, ChronoUnit.DAYS));
        if (!renewAllowed) {
            throw conflict("License cannot be renewed yet");
        }

        int duration = license.getType().getDefaultDurationInDays();
        Instant renewedUntil = endingDate == null || endingDate.isBefore(now)
                ? now.plus(duration, ChronoUnit.DAYS)
                : endingDate.plus(duration, ChronoUnit.DAYS);
        license.setEndingDate(renewedUntil);
        if (license.getUser() != null) {
            license.setStatus(LicenseStatus.ACTIVE);
        }
        licenseRepository.save(license);
        saveHistory(license, currentUser, "RENEWED", "License renewed");

        Device device = deviceRepository.findByMacAddress("renewal")
                .orElse(null);
        return buildTicketResponse(license, device, ticketLifetimeSeconds);
    }

    @Transactional(readOnly = true)
    public TicketResponse checkLicense(String deviceMac, Long productId, int ticketLifetimeSeconds, String username) {
        User currentUser = getUserByUsername(username);
        Device device = deviceRepository.findByMacAddress(normalizeRequired(deviceMac, "Device MAC is required"))
                .orElseThrow(() -> notFound("Device not found"));
        if (!device.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Device belongs to another user");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> notFound("Product not found"));

        License license = licenseRepository.findActiveByDeviceUserAndProduct(device.getId(), currentUser, product, Instant.now())
                .orElseThrow(() -> notFound("License not found"));
        return buildTicketResponse(license, device, ticketLifetimeSeconds);
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> notFound("User not found"));
    }

    private License getLicenseByCode(String activationKey) {
        return licenseRepository.findByCode(normalizeRequired(activationKey, "Activation key is required"))
                .orElseThrow(() -> notFound("License not found"));
    }

    private Device findOrCreateDevice(User user, String deviceMac, String deviceName) {
        String mac = normalizeRequired(deviceMac, "Device MAC is required");
        return deviceRepository.findByMacAddress(mac).map(device -> {
            if (!device.getUser().getId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Device belongs to another user");
            }
            return device;
        }).orElseGet(() -> {
            Device device = new Device();
            device.setUser(user);
            device.setMacAddress(mac);
            device.setName(deviceName == null || deviceName.isBlank() ? mac : deviceName);
            return deviceRepository.save(device);
        });
    }

    private void ensureNotBlocked(License license) {
        if (license.isBlocked() || license.getStatus() == LicenseStatus.BLOCKED) {
            throw conflict("License is blocked");
        }
        if (license.getProduct().isBlocked()) {
            throw conflict("Product is blocked");
        }
    }

    private void saveHistory(License license, User user, String status, String description) {
        LicenseHistory history = new LicenseHistory();
        history.setLicense(license);
        history.setUser(user);
        history.setStatus(status);
        history.setChangeDate(Instant.now());
        history.setDescription(description);
        licenseHistoryRepository.save(history);
    }

    private TicketResponse buildTicketResponse(License license, Device device, int ticketLifetimeSeconds) {
        Ticket ticket = new Ticket();
        ticket.setServerDate(Instant.now());
        ticket.setTicketLifetimeSeconds(ticketLifetimeSeconds);
        ticket.setActivationDate(license.getFirstActivationDate());
        ticket.setExpirationDate(license.getEndingDate());
        ticket.setLicenseCode(license.getCode());
        ticket.setProductId(license.getProduct().getId());
        ticket.setUserId(license.getUser() == null ? null : license.getUser().getId());
        ticket.setDeviceId(device == null ? null : device.getMacAddress());
        ticket.setBlocked(license.isBlocked());

        String signature = digitalSignatureService.signTicket(ticket);
        return new TicketResponse(ticket, signature);
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = randomGroup() + "-" + randomGroup() + "-" + randomGroup() + "-" + randomGroup();
        } while (licenseRepository.existsByCode(code));
        return code;
    }

    private String randomGroup() {
        StringBuilder result = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            result.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
        }
        return result.toString();
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw conflict(message);
        }
        return value.trim();
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}
