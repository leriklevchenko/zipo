package com.example;

import com.example.controller.AntivirusSignatureBinaryController;
import com.example.model.*;
import com.example.service.AntivirusSignatureBinaryService;
import com.example.service.AntivirusSignatureService;
import com.example.service.SignatureBinaryProtocol;
import com.example.signature.DigitalSignatureService;
import com.example.storage.AntivirusSignatureHistoryRepository;
import com.example.storage.AntivirusSignatureRepository;
import com.example.storage.SignatureAuditRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AntivirusSignatureBinaryServiceTest {

    @Autowired
    private AntivirusSignatureService signatureService;

    @Autowired
    private AntivirusSignatureBinaryService binaryService;

    @Autowired
    private AntivirusSignatureBinaryController binaryController;

    @Autowired
    private DigitalSignatureService digitalSignatureService;

    @Autowired
    private AntivirusSignatureRepository signatureRepository;

    @Autowired
    private AntivirusSignatureHistoryRepository historyRepository;

    @Autowired
    private SignatureAuditRepository auditRepository;

    @AfterEach
    void cleanup() {
        auditRepository.deleteAll();
        historyRepository.deleteAll();
        signatureRepository.deleteAll();
    }

    @Test
    void shouldBuildSignedBinaryExportsAndMultipartResponses() throws Exception {
        Instant since = Instant.now().minusSeconds(1);

        signatureService.create("ActiveOne", "1.0", "ACTIVE-PATTERN", "active", "admin");
        AntivirusSignatureResponse deleted = signatureService.create("DeletedOne", "1.0", "DELETED-PATTERN", "deleted", "admin");
        signatureService.delete(deleted.getId(), "admin");

        SignatureBinaryPackage full = binaryService.fullExport();
        ParsedManifest fullManifest = parseManifest(full.getManifest());
        ParsedData fullData = parseData(full.getData());

        assertEquals(SignatureBinaryExportType.FULL.getCode(), fullManifest.exportType);
        assertEquals(1, fullManifest.recordCount);
        assertEquals(1, fullData.statuses.size());
        assertFalse(fullData.statuses.contains(AntivirusSignatureStatus.DELETED));
        assertArrayEquals(sha256(full.getData()), fullManifest.dataSha256);
        assertTrue(digitalSignatureService.verifyManifest(
                full.getManifest(),
                new String(full.getManifestSignature(), StandardCharsets.US_ASCII)
        ));

        SignatureBinaryPackage incremental = binaryService.incrementalExport(since);
        ParsedManifest incrementalManifest = parseManifest(incremental.getManifest());
        ParsedData incrementalData = parseData(incremental.getData());

        assertEquals(SignatureBinaryExportType.INCREMENTAL.getCode(), incrementalManifest.exportType);
        assertEquals(2, incrementalManifest.recordCount);
        assertTrue(incrementalData.statuses.contains(AntivirusSignatureStatus.DELETED));
        assertArrayEquals(sha256(incremental.getData()), incrementalManifest.dataSha256);
        assertTrue(digitalSignatureService.verifyManifest(
                incremental.getManifest(),
                new String(incremental.getManifestSignature(), StandardCharsets.US_ASCII)
        ));

        ResponseEntity<byte[]> response = binaryController.fullExport();
        assertNotNull(response.getHeaders().getContentType());
        assertEquals("multipart", response.getHeaders().getContentType().getType());
        assertEquals("mixed", response.getHeaders().getContentType().getSubtype());

        String multipartBody = new String(response.getBody(), StandardCharsets.ISO_8859_1);
        assertTrue(multipartBody.contains("filename=\"manifest.bin\""));
        assertTrue(multipartBody.contains("filename=\"manifest.sig\""));
        assertTrue(multipartBody.contains("filename=\"signatures.bin\""));
    }

    private ParsedManifest parseManifest(byte[] payload) throws Exception {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(payload))) {
            byte[] magic = in.readNBytes(4);
            assertArrayEquals(SignatureBinaryProtocol.MANIFEST_MAGIC, magic);
            assertEquals(SignatureBinaryProtocol.FORMAT_VERSION, in.readUnsignedShort());

            ParsedManifest manifest = new ParsedManifest();
            manifest.exportType = in.readUnsignedByte();
            manifest.generatedAt = in.readLong();
            manifest.since = in.readLong();
            manifest.recordCount = in.readInt();
            manifest.dataLength = in.readInt();
            manifest.dataSha256 = in.readNBytes(32);
            return manifest;
        }
    }

    private ParsedData parseData(byte[] payload) throws Exception {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(payload))) {
            byte[] magic = in.readNBytes(4);
            assertArrayEquals(SignatureBinaryProtocol.DATA_MAGIC, magic);
            assertEquals(SignatureBinaryProtocol.FORMAT_VERSION, in.readUnsignedShort());
            in.readUnsignedByte();

            ParsedData data = new ParsedData();
            int count = in.readInt();
            for (int i = 0; i < count; i++) {
                in.readLong();
                in.readLong();
                readString(in);
                readString(in);
                readString(in);
                readString(in);
                data.statuses.add(AntivirusSignatureStatus.values()[in.readUnsignedByte()]);
                in.readLong();
                in.readLong();
                readString(in);
            }
            return data;
        }
    }

    private String readString(DataInputStream in) throws Exception {
        int length = in.readInt();
        if (length < 0) {
            return null;
        }
        byte[] bytes = in.readNBytes(length);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private byte[] sha256(byte[] payload) throws Exception {
        return MessageDigest.getInstance("SHA-256").digest(payload);
    }

    private static class ParsedManifest {
        private int exportType;
        private long generatedAt;
        private long since;
        private int recordCount;
        private int dataLength;
        private byte[] dataSha256;
    }

    private static class ParsedData {
        private final List<AntivirusSignatureStatus> statuses = new ArrayList<>();
    }
}
