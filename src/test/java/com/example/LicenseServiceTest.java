package com.example;

import com.example.model.License;
import com.example.model.LicenseType;
import com.example.model.Product;
import com.example.model.TicketResponse;
import com.example.model.User;
import com.example.signature.DigitalSignatureService;
import com.example.service.LicenseService;
import com.example.storage.DeviceLicenseRepository;
import com.example.storage.DeviceRepository;
import com.example.storage.LicenseHistoryRepository;
import com.example.storage.LicenseRepository;
import com.example.storage.LicenseTypeRepository;
import com.example.storage.ProductRepository;
import com.example.storage.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LicenseServiceTest {

    @Autowired
    private LicenseService licenseService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private LicenseTypeRepository licenseTypeRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceLicenseRepository deviceLicenseRepository;

    @Autowired
    private LicenseHistoryRepository licenseHistoryRepository;

    @Autowired
    private DigitalSignatureService digitalSignatureService;

    private User testUser;
    private User adminUser;
    private Product product;
    private LicenseType licenseType;

    @BeforeEach
    void setup() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        adminUser = new User();
        adminUser.setUsername("license_admin");
        adminUser.setPassword(encoder.encode("pass123"));
        adminUser.setRole("ADMIN");
        userRepository.save(adminUser);

        testUser = new User();
        testUser.setUsername("license_user");
        testUser.setPassword(encoder.encode("pass123"));
        testUser.setRole("USER");
        userRepository.save(testUser);

        product = new Product();
        product.setName("Test Antivirus");
        product.setBlocked(false);
        productRepository.save(product);

        licenseType = new LicenseType();
        licenseType.setName("MONTH");
        licenseType.setDefaultDurationInDays(30);
        licenseTypeRepository.save(licenseType);
    }

    @AfterEach
    void cleanup() {
        licenseHistoryRepository.deleteAll();
        deviceLicenseRepository.deleteAll();
        deviceRepository.deleteAll();
        licenseRepository.deleteAll();
        licenseTypeRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createActivateCheckExtendLicense() {
        License license = licenseService.createLicense(product.getId(), licenseType.getId(), testUser.getId(), 2,
                "test license", adminUser.getUsername());
        assertNotNull(license.getId());
        assertNotNull(license.getCode());
        assertEquals(testUser.getId(), license.getOwner().getId());
        assertNull(license.getUser());
        assertEquals(2, license.getDeviceCount());
        assertEquals("CREATED", license.getStatus().name());

        TicketResponse activated = licenseService.activateLicense(license.getCode(), "AA:BB:CC:DD:EE:01", "device-1",
                900, testUser.getUsername());
        assertNotNull(activated.getTicket());
        assertNotNull(activated.getSignature());
        assertTrue(digitalSignatureService.verifyTicket(activated.getTicket(), activated.getSignature()));
        assertEquals(testUser.getId(), activated.getTicket().getUserId());
        assertEquals("AA:BB:CC:DD:EE:01", activated.getTicket().getDeviceId());
        assertFalse(activated.getTicket().isBlocked());
        assertNotNull(activated.getTicket().getActivationDate());
        assertNotNull(activated.getTicket().getExpirationDate());

        TicketResponse checked = licenseService.checkLicense("AA:BB:CC:DD:EE:01", product.getId(), 900, testUser.getUsername());
        assertNotNull(checked.getTicket());
        assertEquals(testUser.getId(), checked.getTicket().getUserId());

        License activatedLicense = licenseRepository.findByCode(license.getCode()).orElseThrow();
        activatedLicense.setEndingDate(Instant.now().plus(3, ChronoUnit.DAYS));
        licenseRepository.save(activatedLicense);

        TicketResponse extended = licenseService.renewLicense(license.getCode(), 900, testUser.getUsername());
        assertNotNull(extended.getTicket());
        assertNotNull(extended.getTicket().getExpirationDate());
        assertTrue(extended.getTicket().getExpirationDate().isAfter(checked.getTicket().getExpirationDate()));
    }
}
