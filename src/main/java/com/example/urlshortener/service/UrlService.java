package com.example.urlshortener.service;

import com.example.urlshortener.model.ShortUrl;

public interface UrlService {
    
    /**
     * Shortens an original URL. Generates a random code if customAlias is null.
     */
    ShortUrl shortenUrl(String originalUrl, String customAlias, Integer expirationHours);

    /**
     * Retrieves the original URL based on the short code. Increments the click counter.
     */
    ShortUrl getOriginalUrl(String shortCode);
}
