package dev.excsi.urlshortener.controller;

import dev.excsi.urlshortener.dto.CreateUrlRequest;
import dev.excsi.urlshortener.dto.CreateUrlResponse;
import dev.excsi.urlshortener.entity.UrlEntity;
import dev.excsi.urlshortener.service.AnalyticsService;
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
public class PublicApiController {

    private final AnalyticsService analyticsService;

    private final UrlHandlerService urlHandlerService;

    public PublicApiController(AnalyticsService analyticsService, UrlHandlerService urlHandlerService) {
        this.analyticsService = analyticsService;
        this.urlHandlerService = urlHandlerService;
    }

    @PostMapping("/shorten")
    public ResponseEntity<CreateUrlResponse> create(@RequestBody CreateUrlRequest request, HttpServletRequest servletRequest) {
        if (request.longUrl() == null || request.longUrl().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid long url");
        }

        String longUrl = request.longUrl().strip();
        Optional<UrlEntity> urlEntity = urlHandlerService.shortenUrl(longUrl);

        if (urlEntity.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not generate a unique short url");
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

    @GetMapping("/{shortUrl:[0-9a-zA-Z]{7}}")
    public ResponseEntity<Void> redirect(@PathVariable String shortUrl) {
        String longUrl = urlHandlerService.getLongUrl(shortUrl);

        analyticsService.recordClick(shortUrl);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, longUrl)
                .build();
    }

//    @GetMapping("/analytics/clicks/{shortUrl:[0-9a-zA-Z]{7}}")
//    public List<DailyClicksResponse> getClickForDays(@PathVariable String shortUrl, @RequestParam(name = "days") int days, OAuth2AuthenticationToken authentication) {
//        UserEntity user = userService.getOrCreateUser(authentication);
//        List<ClickBucketEntity> clickBucketEntities = analyticsService.getClicksForDays(shortUrl, days, user);
//
//        return clickBucketEntities.stream()
//                .map(bucket -> new DailyClicksResponse(bucket.getId().getBucketDate(), bucket.getClicks()))
//                .toList();
//    }
//
//    @GetMapping("/analytics/clicks/{shortUrl:[0-9a-zA-Z]{7}}/total")
//    public TotalClicksResponse getTotalClicks(@PathVariable String shortUrl, OAuth2AuthenticationToken authentication) {
//        UserEntity user = userService.getOrCreateUser(authentication);
//        return new TotalClicksResponse(analyticsService.getTotalClicks(shortUrl, user));
//    }
}
