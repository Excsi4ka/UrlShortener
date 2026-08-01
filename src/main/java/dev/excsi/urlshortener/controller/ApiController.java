package dev.excsi.urlshortener.controller;

import dev.excsi.urlshortener.dto.CreateUrlRequest;
import dev.excsi.urlshortener.dto.CreateUrlResponse;
import dev.excsi.urlshortener.entity.UrlEntity;
import dev.excsi.urlshortener.service.UrlHandlerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@RestController
public class ApiController {

    private final UrlHandlerService urlHandlerService;

    public ApiController(UrlHandlerService urlHandlerService) {
        this.urlHandlerService = urlHandlerService;
    }

    @GetMapping("/{shortUrl:[0-9a-zA-Z]{7}}")
    public ResponseEntity<Void> redirect(@PathVariable String shortUrl) {
        UrlEntity urlEntity = urlHandlerService.getLink(shortUrl).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .header(HttpHeaders.LOCATION, urlEntity.getLongUrl())
                .build();
    }

    @PostMapping("/shorten")
    public ResponseEntity<CreateUrlResponse> create(@RequestBody CreateUrlRequest request, HttpServletRequest servletRequest) {
        if (request.longUrl() == null || request.longUrl().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid long url");
        }

        String longUrl = request.longUrl().strip();
        Optional<UrlEntity> urlEntity = urlHandlerService.shortenUrl(longUrl);

        if (urlEntity.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Could not generate a unique short url");
        }

        String shortUrl = ServletUriComponentsBuilder.fromRequestUri(servletRequest)
                .scheme("https")
                .replacePath(urlEntity.get().getShortCode())
                .replaceQuery(null)
                .build()
                .toUriString();

        URI location = URI.create(shortUrl);
        CreateUrlResponse response = new CreateUrlResponse(shortUrl);

        return ResponseEntity.created(location).body(response);
    }
}
