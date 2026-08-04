package dev.excsi.urlshortener.controller;

import dev.excsi.urlshortener.dto.CreateUrlRequest;
import dev.excsi.urlshortener.dto.CreateUrlResponse;
import dev.excsi.urlshortener.dto.DailyClicksResponse;
import dev.excsi.urlshortener.dto.DashboardLinkResponse;
import dev.excsi.urlshortener.dto.TotalClicksResponse;
import dev.excsi.urlshortener.entity.ClickBucketEntity;
import dev.excsi.urlshortener.entity.UrlEntity;
import dev.excsi.urlshortener.entity.UserEntity;
import dev.excsi.urlshortener.service.AnalyticsService;
import dev.excsi.urlshortener.service.UrlHandlerService;
import dev.excsi.urlshortener.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/dashboard/")
public class PrivateApiController {

    private final AnalyticsService analyticsService;

    private final UrlHandlerService urlHandlerService;

    private final UserService userService;

    public PrivateApiController(AnalyticsService analyticsService, UrlHandlerService urlHandlerService, UserService userService) {
        this.analyticsService = analyticsService;
        this.urlHandlerService = urlHandlerService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<CreateUrlResponse> create(
            @RequestBody CreateUrlRequest request,
            HttpServletRequest servletRequest,
            OAuth2AuthenticationToken authentication
    ) {
        String longUrl = normalizedLongUrl(request);
        UserEntity user = userService.getOrCreateUser(authentication);
        Optional<UrlEntity> urlEntity = urlHandlerService.shortenUrlWithAnalytics(longUrl, user);

        if (urlEntity.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not generate a unique short url");
        }

        String shortUrl = shortUrlFor(servletRequest, urlEntity.get());
        return ResponseEntity.created(URI.create(shortUrl)).body(new CreateUrlResponse(shortUrl));
    }

    @GetMapping
    public List<DashboardLinkResponse> list(HttpServletRequest servletRequest, OAuth2AuthenticationToken authentication) {
        UserEntity user = userService.getOrCreateUser(authentication);
        return urlHandlerService.getLinksForOwner(user).stream()
                .map(url -> dashboardLinkResponse(servletRequest, url))
                .toList();
    }

    @GetMapping("/links/{shortUrl:[0-9a-zA-Z]{7}}/analytics")
    public List<DailyClicksResponse> getClicksForDays(
            @PathVariable String shortUrl,
            @RequestParam(name = "days", defaultValue = "30") int days,
            OAuth2AuthenticationToken authentication
    ) {
        UserEntity user = userService.getOrCreateUser(authentication);
        List<ClickBucketEntity> clickBucketEntities = analyticsService.getClicksForDays(shortUrl, days, user);

        return clickBucketEntities.stream()
                .map(bucket -> new DailyClicksResponse(bucket.getId().getBucketDate(), bucket.getClicks()))
                .toList();
    }

    @GetMapping("/links/{shortUrl:[0-9a-zA-Z]{7}}/analytics/total")
    public TotalClicksResponse getTotalClicks(@PathVariable String shortUrl, OAuth2AuthenticationToken authentication) {
        UserEntity user = userService.getOrCreateUser(authentication);
        return new TotalClicksResponse(analyticsService.getTotalClicks(shortUrl, user));
    }

    private DashboardLinkResponse dashboardLinkResponse(HttpServletRequest request, UrlEntity url) {
        return new DashboardLinkResponse(
                url.getShortCode(),
                shortUrlFor(request, url),
                url.getLongUrl(),
                url.getDateCreated(),
                url.getTotalClicks(),
                url.hasAnalytics()
        );
    }

    private String normalizedLongUrl(CreateUrlRequest request) {
        if (request.longUrl() == null || request.longUrl().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid long url");
        }

        return request.longUrl().strip();
    }

    private String shortUrlFor(HttpServletRequest request, UrlEntity url) {
        return ServletUriComponentsBuilder.fromRequestUri(request)
                .scheme("https")
                .replacePath(url.getShortCode())
                .replaceQuery(null)
                .build()
                .toUriString();
    }
}
