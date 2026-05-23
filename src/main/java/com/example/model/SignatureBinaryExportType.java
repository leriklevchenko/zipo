package com.example.model;

public enum SignatureBinaryExportType {
    FULL(1),
    INCREMENTAL(2);

    private final int code;

    SignatureBinaryExportType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
