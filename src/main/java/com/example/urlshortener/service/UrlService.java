package com.example.urlshortener.service;

import com.example.urlshortener.model.ShortUrl;
import com.example.urlshortener.model.User;

public interface UrlService {
    
    /**
     * Shortens an original URL. Generates a random code if customAlias is null.
     */
    ShortUrl shortenUrl(String originalUrl, String customAlias, Integer expirationHours, User user, String password);

    /**
     * Retrieves the original URL based on the short code. Increments the click counter.
     * Throws PasswordRequiredException if the URL is protected.
     */
    ShortUrl getOriginalUrl(String shortCode);

    /**
     * Unlocks a password-protected URL.
     */
    ShortUrl unlockUrl(String shortCode, String rawPassword);
}
