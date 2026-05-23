package com.example.model;

import java.time.Instant;

public class SignatureBinaryManifest {
    private SignatureBinaryExportType exportType;
    private Instant generatedAt;
    private Instant since;
    private int recordCount;
    private int dataLength;
    private byte[] dataSha256;

    public SignatureBinaryExportType getExportType() { return exportType; }
    public void setExportType(SignatureBinaryExportType exportType) { this.exportType = exportType; }

    public Instant getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }

    public Instant getSince() { return since; }
    public void setSince(Instant since) { this.since = since; }

    public int getRecordCount() { return recordCount; }
    public void setRecordCount(int recordCount) { this.recordCount = recordCount; }

    public int getDataLength() { return dataLength; }
    public void setDataLength(int dataLength) { this.dataLength = dataLength; }

    public byte[] getDataSha256() { return dataSha256; }
    public void setDataSha256(byte[] dataSha256) { this.dataSha256 = dataSha256; }
}
