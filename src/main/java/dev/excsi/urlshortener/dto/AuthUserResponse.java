package dev.excsi.urlshortener.dto;

public record AuthUserResponse(
        Long id,
        String provider,
        String email,
        String displayName,
        String pictureUrl
) {}
