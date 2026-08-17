package com.example.urlshortener.controller;

import com.example.urlshortener.dto.ShortenRequest;
import com.example.urlshortener.dto.ShortenResponse;
import com.example.urlshortener.dto.UnlockRequest;
import com.example.urlshortener.model.ShortUrl;
import com.example.urlshortener.model.User;
import com.example.urlshortener.service.UrlService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allows our future React frontend to call this API without CORS errors
public class UrlController {

    private final UrlService urlService;

    /**
     * Endpoint to create a new short URL.
     */
    @PostMapping("/api/urls/shorten")
    public ResponseEntity<ShortenResponse> shortenUrl(
            @Valid @RequestBody ShortenRequest request, 
            HttpServletRequest httpRequest) {
        
        // Extract authenticated user if available
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = null;
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            user = (User) authentication.getPrincipal();
        }

        // 1. Delegate business logic to the service
        ShortUrl shortUrl = urlService.shortenUrl(request.getOriginalUrl(), request.getCustomAlias(), request.getExpirationHours(), user, request.getPassword());
        
        // 2. Construct the full short URL dynamically based on the current server host
        String baseUrl = httpRequest.getRequestURL().toString().replace(httpRequest.getRequestURI(), "");
        String fullShortUrl = baseUrl + "/" + shortUrl.getShortCode();

        // 3. Map Entity to DTO
        ShortenResponse response = ShortenResponse.builder()
                .shortCode(shortUrl.getShortCode())
                .shortUrl(fullShortUrl)
                .originalUrl(shortUrl.getOriginalUrl())
                .expiresAt(shortUrl.getExpiresAt())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Endpoint to redirect a short code to the original URL.
     */
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirectToOriginal(@PathVariable String shortCode) {
        // Fetches the original URL. If not found, GlobalExceptionHandler handles it.
        ShortUrl shortUrl = urlService.getOriginalUrl(shortCode);
        
        // Return a 302 Found redirect
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(shortUrl.getOriginalUrl()));
        
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    /**
     * Endpoint to unlock a password-protected URL.
     */
    @PostMapping("/{shortCode}/unlock")
    public ResponseEntity<Void> unlockOriginalUrl(@PathVariable String shortCode, @Valid @RequestBody UnlockRequest request) {
        ShortUrl shortUrl = urlService.unlockUrl(shortCode, request.getPassword());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(shortUrl.getOriginalUrl()));
        
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
