package com.tarun.customupload.controller;

import com.tarun.customupload.service.GoogleDriveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.doReturn;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
class GoogleDriveControllerTest {

    //Simulates HTTP requests
    private MockMvc mockMvc;

    @Mock
    private GoogleDriveService driveService;

    @Mock
    private OAuth2AuthenticationToken authentication;

    @InjectMocks
    private GoogleDriveController controller;

    @BeforeEach
    void setup() {

        //Initializes all the @Mock and @InjectMocks
        MockitoAnnotations.openMocks(this);

        //Builds the MockMvc around the GoogleDriveController without using Spring context
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testUploadFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "file content".getBytes()
        );

        ResponseEntity<String> mockResponse = ResponseEntity.ok("Uploaded");

        doReturn(mockResponse)
                .when(driveService)
                .uploadFile(any(), any());

        mockMvc.perform(multipart("/drive/upload")
                        .file(file)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().string("Uploaded"));
    }


    @Test
    void testListFiles() throws Exception {
        ResponseEntity<String> mockResponse = ResponseEntity.ok("{files: []}");

        doReturn(mockResponse)
                .when(driveService)
                .listFiles(any());

        mockMvc.perform(get("/drive/files")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().string("{files: []}"));
    }

    @Test
    void testDeleteFile() throws Exception {
        ResponseEntity<String> mockResponse = ResponseEntity.ok("Deleted");

        doReturn(mockResponse)
                .when(driveService)
                .deleteFile(any(), any());

        mockMvc.perform(post("/drive/delete/{fileId}", "abc123")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().string("Deleted"));
    }


    @Test
    void testDownloadFile() throws Exception {
        byte[] fileContent = "fake content".getBytes();

        ResponseEntity<byte[]> mockResponse = ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header("Content-Disposition", "attachment; filename=\"test.txt\"")
                .body(fileContent);

        doReturn(mockResponse)
                .when(driveService)
                .downloadFile(any(), any());

        mockMvc.perform(get("/drive/download/{fileId}", "abc123")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"test.txt\""))
                .andExpect(content().bytes(fileContent));
    }

}
