package com.example.service;

import com.example.model.AntivirusSignature;
import com.example.model.SignatureBinaryExportType;
import com.example.model.SignatureBinaryManifest;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class SignatureBinaryProtocol {
    public static final int FORMAT_VERSION = 1;
    public static final byte[] MANIFEST_MAGIC = new byte[]{'Z', 'S', 'G', 'M'};
    public static final byte[] DATA_MAGIC = new byte[]{'Z', 'S', 'G', 'D'};

    private SignatureBinaryProtocol() {
    }

    public static byte[] writeManifest(SignatureBinaryManifest manifest) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);
            out.write(MANIFEST_MAGIC);
            out.writeShort(FORMAT_VERSION);
            out.writeByte(manifest.getExportType().getCode());
            out.writeLong(toEpochMillis(manifest.getGeneratedAt()));
            out.writeLong(manifest.getSince() != null ? toEpochMillis(manifest.getSince()) : -1L);
            out.writeInt(manifest.getRecordCount());
            out.writeInt(manifest.getDataLength());
            byte[] hash = manifest.getDataSha256();
            if (hash == null || hash.length != 32) {
                throw new IllegalArgumentException("Manifest SHA-256 hash must contain 32 bytes");
            }
            out.write(hash);
            return bytes.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to serialize signature manifest", e);
        }
    }

    public static byte[] writeData(SignatureBinaryExportType exportType, List<AntivirusSignature> signatures) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);
            out.write(DATA_MAGIC);
            out.writeShort(FORMAT_VERSION);
            out.writeByte(exportType.getCode());
            out.writeInt(signatures.size());
            for (AntivirusSignature signature : signatures) {
                writeUuid(out, signature.getId());
                writeString(out, signature.getName());
                writeString(out, signature.getVersion());
                writeString(out, signature.getPattern());
                writeString(out, signature.getDescription());
                out.writeByte(signature.getStatus().ordinal());
                out.writeLong(toEpochMillis(signature.getCreatedAt()));
                out.writeLong(toEpochMillis(signature.getUpdatedAt()));
                writeString(out, signature.getDigitalSignature());
            }
            return bytes.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to serialize signature data", e);
        }
    }

    private static void writeUuid(DataOutputStream out, UUID value) throws Exception {
        out.writeLong(value.getMostSignificantBits());
        out.writeLong(value.getLeastSignificantBits());
    }

    private static void writeString(DataOutputStream out, String value) throws Exception {
        if (value == null) {
            out.writeInt(-1);
            return;
        }
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    private static long toEpochMillis(Instant value) {
        return value != null ? value.toEpochMilli() : -1L;
    }
}
