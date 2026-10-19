package com.roadresq.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${roadresq.upload.dir:./uploads/problems}")
    private String uploadDir;

    public String storeFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files (JPEG, PNG, WEBP) are accepted");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = ".jpg";
        if (originalFilename != null && originalFilename.contains(".")) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (ext.equals(".png") || ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".webp")) {
                extension = ext;
            }
        }

        // Clean directory path
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String safeFileName = "problem_" + UUID.randomUUID() + extension;
        Path targetLocation = uploadPath.resolve(safeFileName);

        Files.copy(file.getInputStream(), targetLocation);
        return "/uploads/problems/" + safeFileName;
    }

    /**
     * Stores Base64 data URL captured directly from browser camera canvas
     */
    public String storeBase64Image(String base64Data) throws IOException {
        if (base64Data == null || !base64Data.contains(",")) {
            throw new IllegalArgumentException("Invalid base64 image data");
        }

        String[] parts = base64Data.split(",");
        String header = parts[0];
        String data = parts[1];

        String extension = ".jpg";
        if (header.contains("image/png")) {
            extension = ".png";
        } else if (header.contains("image/webp")) {
            extension = ".webp";
        }

        byte[] decodedBytes = Base64.getDecoder().decode(data);

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String safeFileName = "camera_" + UUID.randomUUID() + extension;
        Path targetLocation = uploadPath.resolve(safeFileName);

        try (FileOutputStream fos = new FileOutputStream(targetLocation.toFile())) {
            fos.write(decodedBytes);
        }

        return "/uploads/problems/" + safeFileName;
    }
}
