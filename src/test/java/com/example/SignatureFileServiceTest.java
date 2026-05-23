package com.example;

import com.example.model.*;
import com.example.service.AntivirusSignatureService;
import com.example.service.SignatureFileService;
import com.example.storage.AntivirusSignatureRepository;
import com.example.storage.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SignatureFileServiceTest {

    @Test
    void shouldUploadFileCalculateSha256AndCreateSignature() {
        FileStorageService fileStorageService = mock(FileStorageService.class);
        AntivirusSignatureService signatureService = mock(AntivirusSignatureService.class);
        AntivirusSignatureRepository signatureRepository = mock(AntivirusSignatureRepository.class);
        SignatureFileService service = new SignatureFileService(
                fileStorageService,
                signatureService,
                signatureRepository,
                3600
        );

        StoredSignatureFile storedFile = new StoredSignatureFile(
                "signatures/file.bin",
                "file.bin",
                "application/octet-stream",
                4,
                "9f64a747e1b97f131fabb6b447296c9b6f0201e79fb3c5356e6c77e89b6a806a"
        );
        when(fileStorageService.uploadSignatureFile(anyString(), anyString(), any(byte[].class), anyString()))
                .thenReturn(storedFile);

        AntivirusSignatureResponse expected = new AntivirusSignatureResponse();
        expected.setId(UUID.randomUUID());
        expected.setPattern(storedFile.getSha256());
        expected.setFileObjectKey(storedFile.getObjectKey());
        when(signatureService.createFromFile(eq("file.bin"), eq("1.0"), eq("from file"), any(), eq("admin")))
                .thenReturn(expected);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "file.bin",
                "application/octet-stream",
                new byte[]{1, 2, 3, 4}
        );

        AntivirusSignatureResponse actual = service.uploadAndCreateSignature(
                file,
                null,
                "1.0",
                "from file",
                "admin"
        );

        assertEquals(expected.getId(), actual.getId());
        verify(fileStorageService).uploadSignatureFile(
                eq("file.bin"),
                eq("application/octet-stream"),
                argThat(content -> Arrays.equals(content, new byte[]{1, 2, 3, 4})),
                eq("9f64a747e1b97f131fabb6b447296c9b6f0201e79fb3c5356e6c77e89b6a806a")
        );
        verify(signatureService).createFromFile(eq("file.bin"), eq("1.0"), eq("from file"), same(storedFile), eq("admin"));
    }

    @Test
    void shouldReturnPresignedUrlsOnlyForSignaturesWithStoredFiles() {
        FileStorageService fileStorageService = mock(FileStorageService.class);
        AntivirusSignatureService signatureService = mock(AntivirusSignatureService.class);
        AntivirusSignatureRepository signatureRepository = mock(AntivirusSignatureRepository.class);
        SignatureFileService service = new SignatureFileService(
                fileStorageService,
                signatureService,
                signatureRepository,
                600
        );

        UUID fileSignatureId = UUID.randomUUID();
        AntivirusSignature withFile = new AntivirusSignature();
        withFile.setId(fileSignatureId);
        withFile.setFileObjectKey("signatures/file.bin");
        withFile.setFileOriginalName("file.bin");
        withFile.setFileSize(4L);
        withFile.setFileSha256("hash");

        AntivirusSignature withoutFile = new AntivirusSignature();
        withoutFile.setId(UUID.randomUUID());

        when(signatureRepository.findAllById(anyList())).thenReturn(List.of(withFile, withoutFile));
        when(fileStorageService.presignedGetUrl(eq("signatures/file.bin"), eq(Duration.ofSeconds(600))))
                .thenReturn("http://localhost:9000/ziovpo-signature-files/signatures/file.bin?X-Amz-Signature=test");

        SignatureFilePresignedUrlResponse response = service.presignedUrls(List.of(fileSignatureId, withoutFile.getId()));

        assertEquals(1, response.getUrls().size());
        assertEquals(fileSignatureId, response.getUrls().get(0).getSignatureId());
        assertTrue(response.getUrls().get(0).getUrl().contains("X-Amz-Signature"));
    }
}
