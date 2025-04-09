package com.tarun.customupload.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.tarun.customupload.enums.AppConstants;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Business logic of all API's
 */
@Service
public class GoogleDriveService {

    private final OAuth2AuthorizedClientService clientService;

    public GoogleDriveService(OAuth2AuthorizedClientService clientService) {
        this.clientService = clientService;
    }

    /**
     * Build Google Drive Client from OAuth2 token.
     */
    public Drive buildDriveClient(OAuth2AuthenticationToken auth) throws GeneralSecurityException, IOException {
        var client = clientService.loadAuthorizedClient(
            auth.getAuthorizedClientRegistrationId(),
            auth.getName());

        var accessToken = client.getAccessToken().getTokenValue();

        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);

        return new Drive.Builder(
            com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport(),
            com.google.api.client.json.jackson2.JacksonFactory.getDefaultInstance(),
            credential
        ).setApplicationName(AppConstants.DriveSyncApp.get()).build();
    }

    /**
     * List user files.
     */
    public ResponseEntity<?> listFiles(OAuth2AuthenticationToken auth) {
        try {
            Drive drive = buildDriveClient(auth);
            FileList result = drive.files().list()
                    .setPageSize(100)
                    .setFields("files(id, name)")
                    .execute();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to list files: " + e.getMessage());
        }
    }

    /**
     * Upload file.
     */
    public ResponseEntity<?> uploadFile(OAuth2AuthenticationToken auth, MultipartFile multipartFile) {
        try {
            Drive drive = buildDriveClient(auth);

            //Creates file metadata with the uploaded filename
            File fileMetadata = new File();
            fileMetadata.setName(multipartFile.getOriginalFilename());

            File uploadedFile = drive.files().create(fileMetadata,
                    new com.google.api.client.http.InputStreamContent(
                            multipartFile.getContentType(),
                            multipartFile.getInputStream()))
                    .setFields("id, name")
                    .execute();

            return ResponseEntity.ok(uploadedFile);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }

    /**
     * Download file.
     * @throws GeneralSecurityException 
     */
    public ResponseEntity<byte[]> downloadFile(OAuth2AuthenticationToken auth, String fileId) throws GeneralSecurityException {
        try {
            Drive drive = buildDriveClient(auth);

            //Creates a stream to hold downloaded file bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            //Downloads the file content into the stream
            drive.files().get(fileId).executeMediaAndDownloadTo(outputStream);

            //Fetches metadata like file name and MIME type
            File fileMeta = drive.files().get(fileId).setFields("name, mimeType").execute();

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + fileMeta.getName() + "\"")
                    .header("Content-Type", fileMeta.getMimeType())
                    .body(outputStream.toByteArray());

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete file.
     * @throws GeneralSecurityException 
     */
    public ResponseEntity<?> deleteFile(OAuth2AuthenticationToken auth, String fileId) throws GeneralSecurityException {
        try {
            Drive drive = buildDriveClient(auth);
            drive.files().delete(fileId).execute();
            return ResponseEntity.ok("Deleted file with ID: " + fileId);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Delete failed: " + e.getMessage());
        }
    }
}
