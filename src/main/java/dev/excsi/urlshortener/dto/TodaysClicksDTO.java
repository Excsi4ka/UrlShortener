package dev.excsi.urlshortener.dto;

import java.time.LocalDateTime;

public record TodaysClicksDTO(String shortUrl, String longUrl, LocalDateTime creationDate, long clicks) {
}
