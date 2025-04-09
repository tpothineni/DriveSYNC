package com.tarun.customupload.controller;

import com.tarun.customupload.service.GoogleDriveService;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 *Handles endpoints for getting data from Google Drive
 *
 * GET /drive/files
 *      - get all the files from user's Google drive
 * GET /drive/downlaod/fileid
 *      - download the file from user's Google drive
 * POST /drive/upload
 *      - upload the file into user's Google drive
 * POST /drive/delete/fileid
 *      - delete the file from user's Google drive
 */
@RestController
@RequestMapping("/drive")
public class GoogleDriveController {

    @Autowired
    private GoogleDriveService driveService;

    @GetMapping("/files")
    public ResponseEntity<?> listFiles(OAuth2AuthenticationToken auth) {
        return driveService.listFiles(auth);
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<?> downloadFile(OAuth2AuthenticationToken auth, @PathVariable String fileId) throws GeneralSecurityException {
        return driveService.downloadFile(auth, fileId);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(OAuth2AuthenticationToken auth, @RequestParam("file") MultipartFile file) throws IOException {
        return driveService.uploadFile(auth, file);
    }

    @PostMapping("/delete/{fileId}")
    public ResponseEntity<?> deleteFile(OAuth2AuthenticationToken auth, @PathVariable String fileId) throws GeneralSecurityException {
        return driveService.deleteFile(auth, fileId);
    }
}
