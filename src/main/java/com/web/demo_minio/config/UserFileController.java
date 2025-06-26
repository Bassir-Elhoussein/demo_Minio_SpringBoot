package com.web.demo_minio.config;

/**
 * Author: Bassir El Houssein
 * Date: 6/26/2025
 */
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserFileController {

    private final FileStorageService storageService;

    @PostMapping("/{id}/upload")
    public ResponseEntity<String> uploadProfile(@PathVariable String id,
                                                @RequestParam("file") MultipartFile file) {
        try {
            storageService.uploadUserProfilePicture(id, file);
            String url = storageService.getUserImageUrl(id, file.getOriginalFilename());
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
}
