package com.example.urlshortener.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class ShortCodeGenerator {

    // Base58 character set: a-z, A-Z, 1-9 (excluding l, I, 0, O)
    // This provides 58 possible characters per position and avoids visual confusion.
    private static final String ALPHABET = "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ123456789";
    private static final int BASE = ALPHABET.length();
    
    // We use SecureRandom instead of Random for better cryptographic randomness, 
    // reducing the chance of predictable sequences.
    private final SecureRandom random = new SecureRandom();

    /**
     * Generates a random Base62 string of the specified length.
     * 
     * @param length the number of characters in the generated short code
     * @return the random short code string
     */
    public String generateRandomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(BASE);
            sb.append(ALPHABET.charAt(randomIndex));
        }
        return sb.toString();
    }
}
