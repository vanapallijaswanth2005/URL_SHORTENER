package com.example.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class ShortenRequest {
    
    @NotBlank(message = "Original URL cannot be empty")
    @URL(message = "Must be a valid URL format (e.g., https://example.com)")
    private String originalUrl;

    // Optional custom alias requested by the user
    private String customAlias;

    // Optional expiration time in hours
    private Integer expirationHours;

    // Optional password to protect the link
    private String password;
}
