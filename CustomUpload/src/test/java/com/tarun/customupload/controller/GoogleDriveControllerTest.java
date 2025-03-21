package com.tarun.customupload.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.tarun.customupload.service.GoogleDriveService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

@ExtendWith(SpringExtension.class)
@WebMvcTest(GoogleDriveController.class)
public class GoogleDriveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private GoogleDriveService driveService;

    @InjectMocks
    private GoogleDriveController googleDriveController;

    @Mock
    private OAuth2AuthenticationToken authentication;

    @Test
    public void testListFiles() throws Exception {
        doReturn(ResponseEntity.ok("File List")).when(driveService).listService(any());
        mockMvc.perform(get("/drive/files").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().string("File List"));

        verify(driveService, times(1)).listService(authentication);
    }

    @Test
    public void testUploadFile() throws Exception {
        mockMvc.perform(multipart("/drive/upload")
                        .file("file", "test content".getBytes())
                        .principal(authentication))
                .andExpect(status().is4xxClientError()); // Since we're not providing a full implementation for upload

        verify(driveService, times(0)).uploadService(any(), any());
    }


    @Test
    public void testDownloadFile() throws Exception {
        String fileId = "12345";
        ResponseEntity<Object> responseEntity = ResponseEntity.ok("File Content");

        doReturn(responseEntity).when(driveService).downloadService(any(), eq(fileId));

        mockMvc.perform(get("/drive/files/download/{fileId}", fileId)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().string("File Content"));

        verify(driveService, times(1)).downloadService(any(), eq(fileId));
    }


    @Test
    public void testDeleteFile() throws Exception {
        String fileId = "12345";
        ResponseEntity<Object> responseEntity = ResponseEntity.ok("File Deleted");

        doReturn(responseEntity).when(driveService).deleteService(any(), eq(fileId));

        mockMvc.perform(delete("/drive/files/{fileId}", fileId)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().string("File Deleted"));

        verify(driveService, times(1)).deleteService(any(), eq(fileId));
    }


}
