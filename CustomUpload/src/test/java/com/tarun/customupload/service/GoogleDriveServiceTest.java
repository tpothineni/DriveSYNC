package com.tarun.customupload.service;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) //To allow unused stubs
class GoogleDriveServiceTest {

    @Mock
    private OAuth2AuthorizedClientService clientService;

    @Mock
    private OAuth2AuthenticationToken auth;

    @Mock
    private OAuth2AuthorizedClient authorizedClient;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private GoogleDriveService googleDriveService;

    private OAuth2AccessToken accessToken;

    @BeforeEach
    void setUp() {
        //Creating a fake access token
        accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "mock-token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Set.of("drive")
        );

        //Mocks behavior to simulate loading the token when the service calls buildDriveClient()
        when(auth.getAuthorizedClientRegistrationId()).thenReturn("google");
        when(auth.getName()).thenReturn("test-user");
        when(clientService.loadAuthorizedClient("google", "test-user"))
                .thenReturn(authorizedClient);
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
    }

    @Test
    void testListFiles() throws Exception {
        //Mocks the Google Drive client
        Drive mockDrive = mock(Drive.class);
        Drive.Files mockFiles = mock(Drive.Files.class);
        Drive.Files.List mockList = mock(Drive.Files.List.class);

        //Creating the Sample Response
        File file = new File();
        file.setId("1");
        file.setName("test.txt");
        FileList fileList = new FileList().setFiles(List.of(file));

        //Mocking to Stimulate the Google Drive file list call
        when(mockDrive.files()).thenReturn(mockFiles);
        when(mockFiles.list()).thenReturn(mockList);
        when(mockList.setPageSize(100)).thenReturn(mockList);
        when(mockList.setFields("files(id, name)")).thenReturn(mockList);
        when(mockList.execute()).thenReturn(fileList);

        GoogleDriveService spyService = Mockito.spy(googleDriveService);
        doReturn(mockDrive).when(spyService).buildDriveClient(auth);

        ResponseEntity<?> response = spyService.listFiles(auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(fileList, response.getBody());
    }

    @Test
    void testUploadFile() throws Exception {
        //Mocks the Google Drive client and upload related
        Drive mockDrive = mock(Drive.class);
        Drive.Files mockFiles = mock(Drive.Files.class);
        Drive.Files.Create mockCreate = mock(Drive.Files.Create.class);

        File uploaded = new File();
        uploaded.setId("123");
        uploaded.setName("upload.txt");

        when(mockDrive.files()).thenReturn(mockFiles);
        when(mockFiles.create(any(File.class), any(InputStreamContent.class))).thenReturn(mockCreate);
        when(mockCreate.setFields("id, name")).thenReturn(mockCreate);
        when(mockCreate.execute()).thenReturn(uploaded);

        //Mocks the uploaded file input
        when(multipartFile.getOriginalFilename()).thenReturn("upload.txt");
        when(multipartFile.getContentType()).thenReturn("text/plain");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        GoogleDriveService spyService = Mockito.spy(googleDriveService);
        doReturn(mockDrive).when(spyService).buildDriveClient(auth);

        ResponseEntity<?> response = spyService.uploadFile(auth, multipartFile);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(uploaded, response.getBody());
    }

    @Test
    void testDeleteFile() throws GeneralSecurityException, IOException {
        //Mocks the Google Drive client and Delete related
        Drive mockDrive = mock(Drive.class);
        Drive.Files mockFiles = mock(Drive.Files.class);
        Drive.Files.Delete mockDelete = mock(Drive.Files.Delete.class);

        //Stimulating the responses
        when(mockDrive.files()).thenReturn(mockFiles);
        when(mockFiles.delete("file123")).thenReturn(mockDelete);

        doNothing().when(mockDelete).execute();

        GoogleDriveService spyService = Mockito.spy(googleDriveService);
        doReturn(mockDrive).when(spyService).buildDriveClient(auth);

        ResponseEntity<?> response = spyService.deleteFile(auth, "file123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Deleted file with ID: file123", response.getBody());
    }


    @Test
    void testDownloadFile() throws Exception,GeneralSecurityException {
        //Mocks the Google Drive client and Download related
        Drive mockDrive = mock(Drive.class);
        Drive.Files mockFiles = mock(Drive.Files.class);
        Drive.Files.Get mockGet = mock(Drive.Files.Get.class);

        File meta = new File();
        meta.setName("file.txt");
        meta.setMimeType("text/plain");

        //Mocks file writing into a stream
        when(mockDrive.files()).thenReturn(mockFiles);
        when(mockFiles.get("file123")).thenReturn(mockGet);
        doAnswer(invocation -> {
            ((ByteArrayOutputStream) invocation.getArgument(0)).write("content".getBytes());
            return null;
        }).when(mockGet).executeMediaAndDownloadTo(any(ByteArrayOutputStream.class));
        when(mockGet.setFields("name, mimeType")).thenReturn(mockGet);
        when(mockGet.execute()).thenReturn(meta);

        GoogleDriveService spyService = Mockito.spy(googleDriveService);
        doReturn(mockDrive).when(spyService).buildDriveClient(auth);

        ResponseEntity<byte[]> response = spyService.downloadFile(auth, "file123");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals("content".getBytes(), response.getBody());
    }
}
