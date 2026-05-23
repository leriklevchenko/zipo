package com.example.controller;

import com.example.model.AntivirusSignatureResponse;
import com.example.service.AntivirusSignatureService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/signatures")
public class AntivirusSignatureAdminController {

    private final AntivirusSignatureService signatureService;

    public AntivirusSignatureAdminController(AntivirusSignatureService signatureService) {
        this.signatureService = signatureService;
    }

    @PostMapping
    public ResponseEntity<AntivirusSignatureResponse> create(@RequestBody SignatureRequest request,
                                                             Authentication authentication) {
        return ResponseEntity.ok(signatureService.create(
                request.getName(),
                request.getVersion(),
                request.getPattern(),
                request.getDescription(),
                username(authentication)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AntivirusSignatureResponse> update(@PathVariable UUID id,
                                                             @RequestBody SignatureRequest request,
                                                             Authentication authentication) {
        return ResponseEntity.ok(signatureService.update(
                id,
                request.getName(),
                request.getVersion(),
                request.getPattern(),
                request.getDescription(),
                username(authentication)
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<AntivirusSignatureResponse> delete(@PathVariable UUID id,
                                                             Authentication authentication) {
        return ResponseEntity.ok(signatureService.delete(id, username(authentication)));
    }

    private String username(Authentication authentication) {
        return authentication != null ? authentication.getName() : "system";
    }

    public static class SignatureRequest {
        private String name;
        private String version;
        private String pattern;
        private String description;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }

        public String getPattern() { return pattern; }
        public void setPattern(String pattern) { this.pattern = pattern; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
