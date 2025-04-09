package com.tarun.customupload.integration;

import com.tarun.customupload.controller.GoogleDriveController;
import com.tarun.customupload.service.GoogleDriveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
public class GoogleDriveServiceIntegrationTest {

    private MockMvc mockMvc;

    @Mock
    private GoogleDriveService googleDriveService;

    @InjectMocks
    private GoogleDriveController controller;

    private OAuth2AuthenticationToken mockAuthToken;

    @BeforeEach
    void setUp() throws IOException, GeneralSecurityException {
        //Initializes all the @Mock and @InjectMocks
        MockitoAnnotations.openMocks(this);

        //Builds the MockMvc around the GoogleDriveController without using Spring context
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        Map<String, Object> attributes = Map.of(
                "name", "Test user",
                "email", "testuser@example.com"
        );

        // Create an OAuth2User with the attributes
        OAuth2User principal = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "email"
        );

        // Wrap it in an OAuth2AuthenticationToken
        mockAuthToken = new OAuth2AuthenticationToken(principal, principal.getAuthorities(), "google");

        doReturn(ResponseEntity.ok("{\"files\":[]}"))
                .when(googleDriveService).listFiles(mockAuthToken);

        doReturn(ResponseEntity.ok(Map.of("message", "deleted")))
                .when(googleDriveService).deleteFile(mockAuthToken, "12345");

        doReturn(ResponseEntity.ok("mocked file content".getBytes()))
                .when(googleDriveService).downloadFile(mockAuthToken, "12345");
    }

    @Test
    void testListFilesIntegration() throws Exception {
        mockMvc.perform(get("/drive/files").principal(mockAuthToken))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"files\":[]}"));
    }

    @Test
    void testDeleteFileIntegration() throws Exception {
        mockMvc.perform(post("/drive/delete/12345").principal(mockAuthToken))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\": \"deleted\"}"));
    }

    @Test
    void testDownloadFileIntegration() throws Exception {
        mockMvc.perform(get("/drive/download/12345").principal(mockAuthToken))
                .andExpect(status().isOk())
                .andExpect(content().bytes("mocked file content".getBytes()));
    }

    @Test
    void testUploadFileIntegration() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test content".getBytes()
        );

        doReturn(ResponseEntity.ok("uploaded"))
                .when(googleDriveService).uploadFile(mockAuthToken, file);

        mockMvc.perform(multipart("/drive/upload").file(file).principal(mockAuthToken))
                .andExpect(status().isOk())
                .andExpect(content().string("uploaded"));
    }
}