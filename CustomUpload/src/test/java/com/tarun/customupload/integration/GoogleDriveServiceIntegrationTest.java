package com.tarun.customupload.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class GoogleDriveServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void testListFilesIntegration() throws Exception {
        mockMvc.perform(get("/drive/files"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void testDownloadFileIntegration() throws Exception {
        String fileId = "12345";
        mockMvc.perform(get("/drive/files/download/{fileId}", fileId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void testDeleteFileIntegration() throws Exception {
        String fileId = "12345";
        mockMvc.perform(delete("/drive/files/{fileId}", fileId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void testUploadFileIntegration() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "testFile.txt", "text/plain", "test content".getBytes());

        mockMvc.perform(multipart("/drive/upload").file(mockFile))
                .andExpect(status().isOk());
    }
}
