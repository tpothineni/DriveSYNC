package com.tarun.customupload.service;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.http.*;

@Service
public class GoogleDriveService {


    private final OAuth2AuthorizedClientService authorizedClientService;

    public GoogleDriveService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }
    public String getAccessToken(OAuth2AuthenticationToken authentication){
        // Retrieve the OAuth2 access token.
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                    authentication.getAuthorizedClientRegistrationId(), authentication.getName());
        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        return accessToken;
    }

    public HttpEntity<String> headers(String accessToken){
        // Set up headers with Bearer token ".
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        return entity;
    }

    public ResponseEntity<?> listService(OAuth2AuthenticationToken authentication){

        String accessToken = getAccessToken(authentication);

        RestTemplate restTemplate = new RestTemplate();

        String url = "https://www.googleapis.com/drive/v3/files";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, headers(accessToken), String.class);
        return ResponseEntity.ok(response.getBody());
    }


    public ResponseEntity<?> downloadService(OAuth2AuthenticationToken authentication, String fileId){

        String accessToken = getAccessToken(authentication);

        RestTemplate restTemplate = new RestTemplate();

        // Fetch file metadata (name and mimeType) from Google Drive.
        String metaUrl = "https://www.googleapis.com/drive/v3/files/" + fileId + "?fields=name,mimeType";
        ResponseEntity<Map> metaResponse = restTemplate.exchange(metaUrl, HttpMethod.GET, headers(accessToken), Map.class);
        Map meta = metaResponse.getBody();
        String fileName = (String) meta.get("name");
        String mimeType = (String) meta.get("mimeType");

        // Download the file content using alt=media.
        String downloadUrl = "https://www.googleapis.com/drive/v3/files/" + fileId + "?alt=media";
        ResponseEntity<byte[]> response = restTemplate.exchange(downloadUrl, HttpMethod.GET, headers(accessToken), byte[].class);

        // Set response headers to preserve the file name and MIME type.
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.parseMediaType(mimeType));
        responseHeaders.setContentDisposition(ContentDisposition.builder("attachment").filename(fileName).build());

        return new ResponseEntity<>(response.getBody(), responseHeaders, response.getStatusCode());
    }


    public ResponseEntity<String> uploadService(OAuth2AuthenticationToken authentication, MultipartFile file) throws IOException{

        String accessToken = getAccessToken(authentication);

        String boundary = "foo_bar_baz";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.parseMediaType("multipart/related; boundary=" + boundary));

        // Prepare the metadata JSON (e.g., set the file name).
        String metadata = "{\"name\": \"" + file.getOriginalFilename() + "\"}";

        // Define a boundary for the multipart/related request.
        String lineSeparator = "\r\n";

        // Build the multipart/related request body.
            // We'll use a ByteArrayOutputStream to combine the metadata and file parts.
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8), true);

            // --- Metadata Part ---
            writer.append("--").append(boundary).append(lineSeparator);
            writer.append("Content-Type: application/json; charset=UTF-8").append(lineSeparator);
            writer.append(lineSeparator);
            writer.append(metadata).append(lineSeparator);
            writer.flush();

            // --- File Part ---
            writer.append("--").append(boundary).append(lineSeparator);
            writer.append("Content-Type: ").append(file.getContentType()).append(lineSeparator);
            writer.append(lineSeparator);
            writer.flush();

            // Write the binary file data.
            baos.write(file.getBytes());
            baos.flush();

            // End the multipart request.
            writer.append(lineSeparator).append("--").append(boundary).append("--").append(lineSeparator);
            writer.close();

            byte[] multipartBody = baos.toByteArray();

            // Create the HttpEntity with our multipart/related request body.
            HttpEntity<byte[]> requestEntity = new HttpEntity<>(multipartBody, headers);

            // Google Drive upload URL with uploadType=multipart.
            String uploadUrl = "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart";

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(uploadUrl, requestEntity, String.class);
            return ResponseEntity.ok(response.getBody());
    }


    public ResponseEntity<?> deleteService(OAuth2AuthenticationToken authentication,String fileId){

        String accessToken = getAccessToken(authentication);

        RestTemplate restTemplate = new RestTemplate();

        // Call the Google Drive API to delete the file.
        String url = "https://www.googleapis.com/drive/v3/files/" + fileId;
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, headers(accessToken), String.class);
        return ResponseEntity.ok(response.getBody());
    }
}
