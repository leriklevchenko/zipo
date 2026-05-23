package com.example.controller;

import com.example.model.AntivirusSignatureResponse;
import com.example.model.SignatureFilePresignedUrlRequest;
import com.example.model.SignatureFilePresignedUrlResponse;
import com.example.service.SignatureFileService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/signatures/files")
public class SignatureFileAdminController {
    private final SignatureFileService signatureFileService;

    public SignatureFileAdminController(SignatureFileService signatureFileService) {
        this.signatureFileService = signatureFileService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AntivirusSignatureResponse> upload(@RequestPart("file") MultipartFile file,
                                                             @RequestParam(required = false) String name,
                                                             @RequestParam(defaultValue = "1.0") String version,
                                                             @RequestParam(required = false) String description,
                                                             Authentication authentication) {
        return ResponseEntity.ok(signatureFileService.uploadAndCreateSignature(
                file,
                name,
                version,
                description,
                username(authentication)
        ));
    }

    @PostMapping("/presigned-urls")
    public ResponseEntity<SignatureFilePresignedUrlResponse> presignedUrls(
            @RequestBody SignatureFilePresignedUrlRequest request) {
        return ResponseEntity.ok(signatureFileService.presignedUrls(request.getSignatureIds()));
    }

    private String username(Authentication authentication) {
        return authentication != null ? authentication.getName() : "system";
    }
}
