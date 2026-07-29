package dev.excsi.urlshortener.controller;

import dev.excsi.urlshortener.dto.CreateUrlRequest;
import dev.excsi.urlshortener.dto.CreateUrlResponse;
import dev.excsi.urlshortener.entity.UrlEntity;
import dev.excsi.urlshortener.repository.UrlRepository;
import dev.excsi.urlshortener.service.UrlEncodingService;
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

@RestController
public class UrlController {

    private final UrlEncodingService shortCodeService;

    private final UrlRepository urlRepository;

    public UrlController(UrlEncodingService shortCodeService, UrlRepository urlRepository) {
        this.shortCodeService = shortCodeService;
        this.urlRepository = urlRepository;
    }

    @GetMapping("/{shortCode:[0-9a-f]{7}}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        UrlEntity urlEntity = urlRepository.findById(shortCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

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
        UrlEntity urlEntity = createWithUniqueShortCode(longUrl);

        String shortUrl = ServletUriComponentsBuilder.fromRequestUri(servletRequest)
                .replacePath(urlEntity.getShortCode())
                .replaceQuery(null)
                .build()
                .toUriString();

        URI location = URI.create(shortUrl);
        CreateUrlResponse response = new CreateUrlResponse(shortUrl);

        return ResponseEntity.created(location).body(response);
    }

    private UrlEntity createWithUniqueShortCode(String longUrl) {
        for (int attempt = 0; attempt < 20; attempt++) {
            String shortCode = shortCodeService.shortCodeFor(longUrl, attempt);
            UrlEntity existingUrl = urlRepository.findById(shortCode).orElse(null);
            if (existingUrl != null) {
                if (existingUrl.getLongUrl().equals(longUrl)) {
                    return existingUrl;
                }
                continue;
            }
            return urlRepository.save(new UrlEntity(shortCode, longUrl));
        }

        throw new ResponseStatusException(HttpStatus.CONFLICT, "Could not generate a unique short url");
    }
}
