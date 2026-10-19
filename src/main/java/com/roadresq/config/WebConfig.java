package com.roadresq.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${roadresq.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Value("${roadresq.upload.dir:./uploads/problems}")
    private String uploadDir;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /uploads/** to the absolute filesystem location of ./uploads/
        java.nio.file.Path uploadPath = Paths.get("./uploads").toAbsolutePath().normalize();
        File dir = uploadPath.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File problemsDir = new File(dir, "problems");
        if (!problemsDir.exists()) {
            problemsDir.mkdirs();
        }
        String uploadAbsolutePath = uploadPath.toUri().toString();
        if (!uploadAbsolutePath.endsWith("/")) {
            uploadAbsolutePath += "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadAbsolutePath);
    }
}
