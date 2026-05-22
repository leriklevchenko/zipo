package com.example;

import com.example.model.License;
import com.example.model.TicketResponse;
import com.example.model.User;
import com.example.service.LicenseService;
import com.example.storage.LicenseRepository;
import com.example.storage.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LicenseServiceTest {

    @Autowired
    private LicenseService licenseService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LicenseRepository licenseRepository;

    private User testUser;

    @BeforeEach
    void setup() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        testUser = new User();
        testUser.setUsername("license_user");
        testUser.setPassword(encoder.encode("pass123"));
        testUser.setRole("USER");
        userRepository.save(testUser);
    }

    @AfterEach
    void cleanup() {
        licenseRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createActivateCheckExtendLicense() {
        License license = licenseService.createLicense(testUser.getId(), "device-1", 30);
        assertNotNull(license.getId());
        assertEquals(testUser.getId(), license.getUser().getId());
        assertEquals("device-1", license.getDeviceId());
        assertEquals(30, license.getValidityDays());
        assertEquals("CREATED", license.getStatus().name());

        TicketResponse activated = licenseService.activateLicense(license.getId(), 900);
        assertNotNull(activated.getTicket());
        assertNotNull(activated.getSignature());
        assertEquals(testUser.getId(), activated.getTicket().getUserId());
        assertEquals("device-1", activated.getTicket().getDeviceId());
        assertFalse(activated.getTicket().isBlocked());
        assertNotNull(activated.getTicket().getActivationDate());
        assertNotNull(activated.getTicket().getExpirationDate());

        TicketResponse checked = licenseService.checkLicense(license.getId(), 900);
        assertNotNull(checked.getTicket());
        assertEquals(testUser.getId(), checked.getTicket().getUserId());

        TicketResponse extended = licenseService.extendLicense(license.getId(), 10, 900);
        assertNotNull(extended.getTicket());
        assertNotNull(extended.getTicket().getExpirationDate());
        assertTrue(extended.getTicket().getExpirationDate().isAfter(checked.getTicket().getExpirationDate()));
    }
}
