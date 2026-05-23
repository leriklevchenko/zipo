package com.example.model;

import java.util.List;
import java.util.UUID;

public class SignatureFilePresignedUrlRequest {
    private List<UUID> signatureIds;

    public List<UUID> getSignatureIds() { return signatureIds; }
    public void setSignatureIds(List<UUID> signatureIds) { this.signatureIds = signatureIds; }
}
