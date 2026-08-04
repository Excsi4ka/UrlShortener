package dev.excsi.urlshortener.dto;

import java.time.LocalDateTime;

public record DashboardLinkResponse(
        String shortCode,
        String shortUrl,
        String longUrl,
        LocalDateTime dateCreated,
        int totalClicks,
        boolean analyticsEnabled
) {}
