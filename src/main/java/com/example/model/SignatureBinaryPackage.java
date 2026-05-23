package com.example.model;

public class SignatureBinaryPackage {
    private final byte[] manifest;
    private final byte[] manifestSignature;
    private final byte[] data;
    private final int recordCount;

    public SignatureBinaryPackage(byte[] manifest, byte[] manifestSignature, byte[] data, int recordCount) {
        this.manifest = manifest;
        this.manifestSignature = manifestSignature;
        this.data = data;
        this.recordCount = recordCount;
    }

    public byte[] getManifest() { return manifest; }
    public byte[] getManifestSignature() { return manifestSignature; }
    public byte[] getData() { return data; }
    public int getRecordCount() { return recordCount; }
}
