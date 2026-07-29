package dev.excsi.urlshortener.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class UrlEncodingService {

    private static final int SHORT_URL_LENGTH = 7;

    public String shortCodeFor(String longUrl, int attempt) {
        if (longUrl == null || longUrl.isBlank()) {
            throw new IllegalArgumentException("longUrl must not be blank");
        }
        if (attempt < 0) {
            throw new IllegalArgumentException("attempt must be non-negative");
        }

        return sha256Hex(longUrl + ":" + attempt).substring(0, SHORT_URL_LENGTH);
    }

    private String sha256Hex(String value) {
        byte[] digest = sha256(value);
        StringBuilder hex = new StringBuilder(digest.length * 2);

        for (byte b : digest) {
            hex.append(String.format("%02x", b));
        }

        return hex.toString();
    }

    private byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
