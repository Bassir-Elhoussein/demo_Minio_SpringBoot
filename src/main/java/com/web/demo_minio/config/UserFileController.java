package com.web.demo_minio.config;

/**
 * Author: Bassir El Houssein
 * Date: 6/26/2025
 */

import io.minio.GetObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import jakarta.servlet.http.HttpServletRequest;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserFileController {

    private final FileStorageService storageService;



    @PostMapping("/{id}/upload")
    public ResponseEntity<String> uploadProfile(@PathVariable String id,
                                                @RequestParam("file") MultipartFile file,
                                                HttpServletRequest request) {
        try {
            storageService.uploadUserProfilePicture(id, file);

            // Dynamically build the base URL
            String baseUrl = String.format("%s://%s:%d",
                    request.getScheme(),
                    request.getServerName(),
                    request.getServerPort()
            );

            // Build the proxy image URL
            String url = String.format("%s/api/users/%s/image-proxy/%s", baseUrl, id, file.getOriginalFilename());

            return ResponseEntity.ok("File uploaded: " + url);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }


    @GetMapping("/{id}/image/{fileName}")
    public ResponseEntity<String> getUserImage(@PathVariable String id,
                                               @PathVariable String fileName) {
        try {
            String url = storageService.getUserImageUrl(id, fileName);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error generating URL: " + e.getMessage());
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Controller is reachable");
    }
    @GetMapping("/{id}/image-proxy/{fileName:.+}")
    public ResponseEntity<byte[]> getUserImageProxy(@PathVariable String id,
                                                    @PathVariable String fileName) {
        try (InputStream stream = storageService.getUserImageStream(id, fileName)) {
            System.out.println("Proxy called for user: " + id + ", file: " + fileName);

            String contentType = storageService.getContentType(fileName);

            byte[] imageBytes = stream.readAllBytes();

            System.out.println("Successfully read image bytes: " + imageBytes.length);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(imageBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }


}
