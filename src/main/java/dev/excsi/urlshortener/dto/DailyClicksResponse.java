package dev.excsi.urlshortener.dto;

import java.time.LocalDate;

public record DailyClicksResponse(LocalDate date, int clicks) {
}
