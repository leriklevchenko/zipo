package com.example;

import com.example.model.Ticket;
import com.example.signature.DigitalSignatureService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class DigitalSignatureServiceTest {

    @Autowired
    private DigitalSignatureService digitalSignatureService;

    @Test
    void shouldSignAndVerifyTicket() {
        Ticket ticket = buildTicket();

        String signature = digitalSignatureService.signTicket(ticket);

        assertNotNull(signature);
        assertTrue(digitalSignatureService.verifyTicket(ticket, signature));
    }

    @Test
    void shouldRejectTamperedTicket() {
        Ticket ticket = buildTicket();
        String signature = digitalSignatureService.signTicket(ticket);

        ticket.setBlocked(true);

        assertFalse(digitalSignatureService.verifyTicket(ticket, signature));
    }

    private Ticket buildTicket() {
        Ticket ticket = new Ticket();
        ticket.setServerDate(Instant.parse("2026-05-23T08:00:00Z"));
        ticket.setTicketLifetimeSeconds(900);
        ticket.setActivationDate(Instant.parse("2026-05-23T08:00:00Z"));
        ticket.setExpirationDate(Instant.parse("2026-06-22T08:00:00Z"));
        ticket.setUserId(1L);
        ticket.setDeviceId("device-1");
        ticket.setBlocked(false);
        return ticket;
    }
}
