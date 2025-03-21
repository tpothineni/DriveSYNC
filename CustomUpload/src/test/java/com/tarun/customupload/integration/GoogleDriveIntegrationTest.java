package com.tarun.customupload.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class GoogleDriveIntegrationTest {

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
    public void testUploadFileIntegration() throws Exception {
        mockMvc.perform(multipart("/drive/upload")
                        .file("file", "test content".getBytes()))
                .andExpect(status().is4xxClientError());
    }
}
