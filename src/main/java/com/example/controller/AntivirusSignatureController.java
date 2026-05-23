package com.example.controller;

import com.example.model.AntivirusSignatureHistoryResponse;
import com.example.model.AntivirusSignatureResponse;
import com.example.model.SignatureAuditResponse;
import com.example.service.AntivirusSignatureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user/signatures")
public class AntivirusSignatureController {

    private final AntivirusSignatureService signatureService;

    public AntivirusSignatureController(AntivirusSignatureService signatureService) {
        this.signatureService = signatureService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<AntivirusSignatureResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(signatureService.get(id));
    }

    @GetMapping("/full")
    public ResponseEntity<List<AntivirusSignatureResponse>> fullExport() {
        return ResponseEntity.ok(signatureService.fullExport());
    }

    @GetMapping("/incremental")
    public ResponseEntity<List<AntivirusSignatureResponse>> incrementalExport(@RequestParam Instant since) {
        return ResponseEntity.ok(signatureService.incrementalExport(since));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<AntivirusSignatureHistoryResponse>> history(@PathVariable UUID id) {
        return ResponseEntity.ok(signatureService.history(id));
    }

    @GetMapping("/{id}/audit")
    public ResponseEntity<List<SignatureAuditResponse>> audit(@PathVariable UUID id) {
        return ResponseEntity.ok(signatureService.audit(id));
    }
}
