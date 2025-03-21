package com.tarun.customupload.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@ExtendWith(SpringExtension.class)
public class GoogleDriveServiceTest {

    @Mock
    private OAuth2AuthorizedClientService authorizedClientService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private OAuth2AuthenticationToken authentication;

    @Mock
    private OAuth2AuthorizedClient authorizedClient;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private GoogleDriveService googleDriveService;

    private OAuth2AccessToken mockAccessToken;

    @BeforeEach
    void setUp() {
        mockAccessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "mockAccessToken",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Set.of("read", "write")
        );
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn("google");
        when(authentication.getName()).thenReturn("testUser");
        when(authorizedClientService.loadAuthorizedClient("google", "testUser")).thenReturn(authorizedClient);
        when(authorizedClient.getAccessToken()).thenReturn(mockAccessToken);
    }

    @Test
    public void testGetAccessToken() {
        String token = googleDriveService.getAccessToken(authentication);
        assertEquals("mockAccessToken", token);
    }

    @Test
    public void testListFiles() {
        ResponseEntity<String> mockResponse = ResponseEntity.ok("{\"files\": []}");
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockResponse);

        ResponseEntity<?> response = googleDriveService.listService(authentication);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"files\": []}", response.getBody());
    }

    @Test
    public void testDownloadFile() {
        String fileId = "12345";
        Map<String, String> metaData = new HashMap<>();
        metaData.put("name", "testFile.txt");
        metaData.put("mimeType", "text/plain");

        ResponseEntity<Map> metaResponse = ResponseEntity.ok(metaData);
        ResponseEntity<byte[]> fileResponse = ResponseEntity.ok("file content".getBytes());

        when(restTemplate.exchange(contains(fileId), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(metaResponse);

        when(restTemplate.exchange(contains(fileId), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(fileResponse);

        ResponseEntity<?> response = googleDriveService.downloadService(authentication, fileId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals("file content".getBytes(), (byte[]) response.getBody());
    }

    @Test
    public void testDeleteFile() {
        String fileId = "12345";
        ResponseEntity<String> mockResponse = ResponseEntity.ok("Deleted");

        when(restTemplate.exchange(contains(fileId), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockResponse);

        ResponseEntity<?> response = googleDriveService.deleteService(authentication, fileId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Deleted", response.getBody());
    }

    @Test
    public void testUploadFile() throws IOException {
        when(file.getOriginalFilename()).thenReturn("testFile.txt");
        when(file.getContentType()).thenReturn("text/plain");
        when(file.getBytes()).thenReturn("test content".getBytes());

        ResponseEntity<String> mockResponse = ResponseEntity.ok("{\"id\": \"12345\"}");
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockResponse);

        ResponseEntity<String> response = googleDriveService.uploadService(authentication, file);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"id\": \"12345\"}", response.getBody());
    }
}
