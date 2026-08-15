package com.example.urlshortener.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ShortenResponse {
    private String shortCode;
    private String shortUrl; // The fully qualified URL (e.g., http://localhost:8080/aB3)
    private String originalUrl;
    private LocalDateTime expiresAt;
}
