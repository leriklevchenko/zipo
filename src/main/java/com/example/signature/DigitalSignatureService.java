package com.example.signature;

import com.example.model.Ticket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.time.Instant;
import java.util.Base64;

@Service
public class DigitalSignatureService {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final String signatureAlgorithm;

    public DigitalSignatureService(
            @Value("${eds.keystore.base64:}") String keyStoreBase64,
            @Value("${eds.keystore.location:}") Resource keyStoreLocation,
            @Value("${eds.keystore.password}") String keyStorePassword,
            @Value("${eds.keystore.key-password:${eds.keystore.password}}") String keyPassword,
            @Value("${eds.keystore.alias}") String keyAlias,
            @Value("${eds.signature-algorithm:SHA256withRSA}") String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;

        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (InputStream inputStream = openKeyStore(keyStoreBase64, keyStoreLocation)) {
                keyStore.load(inputStream, keyStorePassword.toCharArray());
            }

            Key key = keyStore.getKey(keyAlias, keyPassword.toCharArray());
            if (!(key instanceof PrivateKey loadedPrivateKey)) {
                throw new IllegalStateException("EDS private key is not found for alias: " + keyAlias);
            }

            Certificate certificate = keyStore.getCertificate(keyAlias);
            if (certificate == null) {
                throw new IllegalStateException("EDS certificate is not found for alias: " + keyAlias);
            }

            this.privateKey = loadedPrivateKey;
            this.publicKey = certificate.getPublicKey();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to initialize EDS key store", e);
        }
    }

    public String signTicket(Ticket ticket) {
        return signData(canonicalTicketPayload(ticket));
    }

    public boolean verifyTicket(Ticket ticket, String signatureValue) {
        return verifyData(canonicalTicketPayload(ticket), signatureValue);
    }

    public String signData(byte[] payload) {
        try {
            Signature signature = Signature.getInstance(signatureAlgorithm);
            signature.initSign(privateKey);
            signature.update(payload);
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to sign payload", e);
        }
    }

    public boolean verifyData(byte[] payload, String signatureValue) {
        try {
            Signature signature = Signature.getInstance(signatureAlgorithm);
            signature.initVerify(publicKey);
            signature.update(payload);
            return signature.verify(Base64.getDecoder().decode(signatureValue));
        } catch (Exception e) {
            return false;
        }
    }

    byte[] canonicalTicketPayload(Ticket ticket) {
        String payload = "{"
                + "\"serverDate\":" + nullableNumber(toEpochMillis(ticket.getServerDate())) + ","
                + "\"ticketLifetimeSeconds\":" + ticket.getTicketLifetimeSeconds() + ","
                + "\"activationDate\":" + nullableNumber(toEpochMillis(ticket.getActivationDate())) + ","
                + "\"expirationDate\":" + nullableNumber(toEpochMillis(ticket.getExpirationDate())) + ","
                + "\"userId\":" + nullableNumber(ticket.getUserId()) + ","
                + "\"deviceId\":\"" + escapeJson(ticket.getDeviceId()) + "\","
                + "\"blocked\":" + ticket.isBlocked()
                + "}";
        return payload.getBytes(StandardCharsets.UTF_8);
    }

    private InputStream openKeyStore(String keyStoreBase64, Resource keyStoreLocation) throws Exception {
        if (keyStoreBase64 != null && !keyStoreBase64.isBlank()) {
            return new ByteArrayInputStream(Base64.getDecoder().decode(keyStoreBase64));
        }
        if (keyStoreLocation != null && keyStoreLocation.exists()) {
            return keyStoreLocation.getInputStream();
        }
        throw new IllegalStateException("EDS key store is not configured");
    }

    private Long toEpochMillis(Instant value) {
        return value != null ? value.toEpochMilli() : null;
    }

    private String nullableNumber(Number value) {
        return value != null ? value.toString() : "null";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
