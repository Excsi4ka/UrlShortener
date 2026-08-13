package dev.excsi.urlshortener.controller;

import dev.excsi.urlshortener.service.AsyncClickEventService;
import dev.excsi.urlshortener.service.UrlHandlerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicApiController {

    private final AsyncClickEventService asyncClickEventService;

    private final UrlHandlerService urlHandlerService;

    public PublicApiController(AsyncClickEventService asyncClickEventService, UrlHandlerService urlHandlerService) {
        this.asyncClickEventService = asyncClickEventService;
        this.urlHandlerService = urlHandlerService;
    }

    @GetMapping("/{shortUrl:[0-9a-zA-Z]{7}}")
    public ResponseEntity<Void> redirect(@PathVariable String shortUrl, HttpServletRequest httpServletRequest) {
        String longUrl = urlHandlerService.getLongUrl(shortUrl);

        asyncClickEventService.recordClick(
                shortUrl,
                httpServletRequest.getHeader("CF-IPCountry"),
                httpServletRequest.getHeader("User-Agent"),
                httpServletRequest.getHeader("Sec-CH-UA-Mobile"),
                httpServletRequest.getHeader("Sec-CH-UA-Platform")
        );

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, longUrl)
                .build();
    }
}
