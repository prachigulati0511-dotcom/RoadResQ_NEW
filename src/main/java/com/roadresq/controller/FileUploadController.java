package com.roadresq.controller;

import com.roadresq.service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    public FileUploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/problem-photo")
    public ResponseEntity<?> uploadProblemPhoto(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = fileStorageService.storeFile(file);
            return ResponseEntity.ok(Map.of("photoUrl", fileUrl, "message", "Photo uploaded successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to store image: " + e.getMessage()));
        }
    }

    @PostMapping("/camera-photo")
    public ResponseEntity<?> uploadCameraPhoto(@RequestBody Map<String, String> payload) {
        String base64Data = payload.get("image");
        if (base64Data == null || base64Data.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Image payload is required"));
        }
        try {
            String fileUrl = fileStorageService.storeBase64Image(base64Data);
            return ResponseEntity.ok(Map.of("photoUrl", fileUrl, "message", "Camera snapshot captured and uploaded successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to decode camera capture: " + e.getMessage()));
        }
    }
}
