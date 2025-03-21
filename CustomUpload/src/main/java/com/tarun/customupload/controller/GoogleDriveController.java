package com.tarun.customupload.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.tarun.customupload.service.GoogleDriveService;

@RestController
@RequestMapping("/drive")
public class GoogleDriveController {

    private final OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    GoogleDriveService driveService;

    public GoogleDriveController(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    /**
     * List files from Google Drive.
     */
    @GetMapping("/files")
    public ResponseEntity<?> listFiles(OAuth2AuthenticationToken authentication) {
        try {
            
            return driveService.listService(authentication);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error listing files: " + e.getMessage());
        }
    }

    @GetMapping("/files/download/{fileId}")
    public ResponseEntity<?> downloadFile(OAuth2AuthenticationToken authentication,
                                      @PathVariable("fileId") String fileId) {
        try {
            return driveService.downloadService(authentication, fileId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error downloading file: " + e.getMessage());
        }
    }



    /**
     * Upload a file to Google Drive.
     */

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(OAuth2AuthenticationToken authentication,
                                             @RequestParam("file") MultipartFile file) {
        try {
            return driveService.uploadService(authentication, file);
        }
        catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("I/O Error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading file: " + e.getMessage());
        }
    }

    /**
     * Delete a file from Google Drive.
     */
    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<?> deleteFile(OAuth2AuthenticationToken authentication,
                                        @PathVariable("fileId") String fileId) {
        try {
            return driveService.deleteService(authentication, fileId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting file: " + e.getMessage());
        }
    }
}
