package com.example.urlshortener.repository;

import com.example.urlshortener.model.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<ShortUrl, Long> {

    /**
     * Finds a ShortUrl by its unique short code.
     * Used during the redirect phase.
     */
    Optional<ShortUrl> findByShortCode(String shortCode);

    /**
     * Checks if a given short code already exists in the database.
     * Useful for validating custom aliases or checking for collisions.
     */
    boolean existsByShortCode(String shortCode);
}
