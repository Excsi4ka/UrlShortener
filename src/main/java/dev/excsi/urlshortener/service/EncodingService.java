package dev.excsi.urlshortener.service;

import io.seruco.encoding.base62.Base62;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class EncodingService {

    private static final int SHORT_URL_LENGTH = 7;

    public String shortCodeFor(String longUrl, int attempt) {
        if (longUrl == null || longUrl.isBlank()) {
            throw new IllegalArgumentException("longUrl must not be blank");
        }
        if (attempt < 0) {
            throw new IllegalArgumentException("attempt must be non-negative");
        }

        return base62Encoding(longUrl + ":" + attempt).substring(0, SHORT_URL_LENGTH);
    }

    private String base62Encoding(String value) {
        byte[] hash = sha256Hash(value);
        Base62 encoding = Base62.createInstance();
        return new String(encoding.encode(hash));
    }

    private byte[] sha256Hash(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
