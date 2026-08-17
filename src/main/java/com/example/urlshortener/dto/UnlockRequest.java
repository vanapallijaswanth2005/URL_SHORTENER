package com.example.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UnlockRequest {
    @NotBlank(message = "Password is required to unlock this link")
    private String password;
}
