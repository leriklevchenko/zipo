package com.example.controller;

import com.example.model.SignatureBinaryPackage;
import com.example.service.AntivirusSignatureBinaryService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/user/signatures/binary")
public class AntivirusSignatureBinaryController {
    private final AntivirusSignatureBinaryService binaryService;

    public AntivirusSignatureBinaryController(AntivirusSignatureBinaryService binaryService) {
        this.binaryService = binaryService;
    }

    @GetMapping("/full")
    public ResponseEntity<byte[]> fullExport() {
        return multipart(binaryService.fullExport(), "full");
    }

    @GetMapping("/incremental")
    public ResponseEntity<byte[]> incrementalExport(@RequestParam Instant since) {
        return multipart(binaryService.incrementalExport(since), "incremental");
    }

    private ResponseEntity<byte[]> multipart(SignatureBinaryPackage binaryPackage, String exportName) {
        String boundary = "ziovpo-" + UUID.randomUUID();
        byte[] body = writeMultipart(boundary, binaryPackage);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "multipart/mixed; boundary=" + boundary)
                .header("X-Signature-Record-Count", String.valueOf(binaryPackage.getRecordCount()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"signatures-" + exportName + ".multipart\"")
                .cacheControl(CacheControl.noStore())
                .body(body);
    }

    private byte[] writeMultipart(String boundary, SignatureBinaryPackage binaryPackage) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            writePart(out, boundary, "manifest", "manifest.bin", "application/octet-stream", binaryPackage.getManifest());
            writePart(out, boundary, "manifest-signature", "manifest.sig", "text/plain", binaryPackage.getManifestSignature());
            writePart(out, boundary, "data", "signatures.bin", "application/octet-stream", binaryPackage.getData());
            out.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.US_ASCII));
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to write multipart binary signature response", e);
        }
    }

    private void writePart(ByteArrayOutputStream out,
                           String boundary,
                           String name,
                           String filename,
                           String contentType,
                           byte[] payload) throws Exception {
        out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.US_ASCII));
        out.write(("Content-Disposition: attachment; name=\"" + name + "\"; filename=\"" + filename + "\"\r\n")
                .getBytes(StandardCharsets.US_ASCII));
        out.write(("Content-Type: " + contentType + "\r\n").getBytes(StandardCharsets.US_ASCII));
        out.write(("Content-Length: " + payload.length + "\r\n\r\n").getBytes(StandardCharsets.US_ASCII));
        out.write(payload);
        out.write("\r\n".getBytes(StandardCharsets.US_ASCII));
    }
}
