package dev.excsi.urlshortener.controller;

import dev.excsi.urlshortener.service.AnalyticsService;
import dev.excsi.urlshortener.service.UrlHandlerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedirectController {

    private final UrlHandlerService urlHandlerService;

    private final AnalyticsService analyticsService;

    public RedirectController(UrlHandlerService urlHandlerService, AnalyticsService analyticsService) {
        this.urlHandlerService = urlHandlerService;
        this.analyticsService = analyticsService;
    }

    @GetMapping("/{shortUrl:[0-9a-zA-Z]{7}}")
    public ResponseEntity<Void> redirect(@PathVariable String shortUrl, HttpServletRequest servletRequest) {
        String longUrl = urlHandlerService.getLongUrl(shortUrl);

        analyticsService.recordClick(shortUrl);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, longUrl)
                .build();
    }
}
