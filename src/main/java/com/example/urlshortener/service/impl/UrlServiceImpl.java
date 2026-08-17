package com.example.urlshortener.service.impl;

import com.example.urlshortener.exception.AliasAlreadyTakenException;
import com.example.urlshortener.exception.UrlExpiredException;
import com.example.urlshortener.exception.UrlNotFoundException;
import com.example.urlshortener.exception.PasswordRequiredException;
import com.example.urlshortener.exception.InvalidPasswordException;
import com.example.urlshortener.model.ShortUrl;
import com.example.urlshortener.model.User;
import com.example.urlshortener.repository.UrlRepository;
import com.example.urlshortener.service.UrlService;
import com.example.urlshortener.util.ShortCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

    private final UrlRepository urlRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public ShortUrl shortenUrl(String originalUrl, String customAlias, Integer expirationHours, User user, String password) {
        // 1. Handle Custom Alias
        if (customAlias != null && !customAlias.isBlank()) {
            if (urlRepository.existsByShortCode(customAlias)) {
                throw new AliasAlreadyTakenException("The alias '" + customAlias + "' is already in use.");
            }
            return saveUrl(originalUrl, customAlias, expirationHours, user, password);
        }

        // 2. Handle Random Generation
        String generatedCode;
        int maxRetries = 5;
        int attempts = 0;
        
        do {
            generatedCode = shortCodeGenerator.generateRandomCode(7);
            attempts++;
            if (attempts > maxRetries) {
                throw new RuntimeException("Failed to generate a unique short code after " + maxRetries + " attempts.");
            }
            // We loop here just in case our random generator happens to pick 
            // a 7-character string that is already in the database.
        } while (urlRepository.existsByShortCode(generatedCode));

        return saveUrl(originalUrl, generatedCode, expirationHours, user, password);
    }

    private ShortUrl saveUrl(String originalUrl, String shortCode, Integer expirationHours, User user, String password) {
        ShortUrl shortUrl = ShortUrl.builder()
                .originalUrl(originalUrl)
                .shortCode(shortCode)
                .active(true)
                .user(user)
                .build();
                
        if (password != null && !password.isBlank()) {
            shortUrl.setPassword(passwordEncoder.encode(password));
        }

        if (expirationHours != null && expirationHours > 0) {
            shortUrl.setExpiresAt(LocalDateTime.now().plusHours(expirationHours));
        }
        
        return urlRepository.save(shortUrl);
    }

    @Override
    @Transactional
    public ShortUrl getOriginalUrl(String shortCode) {
        // Fetch from DB or throw a 404-equivalent exception
        ShortUrl shortUrl = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("Short URL '" + shortCode + "' not found."));
        
        // Soft delete check
        if (!shortUrl.isActive()) {
            throw new UrlNotFoundException("Short URL '" + shortCode + "' is inactive.");
        }
        
        // Expiration check
        if (shortUrl.getExpiresAt() != null && shortUrl.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UrlExpiredException("This short URL has expired.");
        }
        
        // Password protection check
        if (shortUrl.getPassword() != null) {
            throw new PasswordRequiredException("This URL is password protected.");
        }
        
        // Click tracking: increment the counter
        shortUrl.setClickCount(shortUrl.getClickCount() + 1);
        
        // Returning the modified entity inside a @Transactional method 
        // causes Hibernate to automatically UPDATE the row in the DB at the end of the transaction.
        return shortUrl;
    }

    @Override
    @Transactional
    public ShortUrl unlockUrl(String shortCode, String rawPassword) {
        ShortUrl shortUrl = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("Short URL '" + shortCode + "' not found."));

        if (!shortUrl.isActive()) {
            throw new UrlNotFoundException("Short URL '" + shortCode + "' is inactive.");
        }

        if (shortUrl.getExpiresAt() != null && shortUrl.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UrlExpiredException("This short URL has expired.");
        }

        if (shortUrl.getPassword() == null) {
            // URL isn't protected, just return it
            shortUrl.setClickCount(shortUrl.getClickCount() + 1);
            return shortUrl;
        }

        if (!passwordEncoder.matches(rawPassword, shortUrl.getPassword())) {
            throw new InvalidPasswordException("Incorrect password.");
        }

        shortUrl.setClickCount(shortUrl.getClickCount() + 1);
        return shortUrl;
    }
}
