package dev.excsi.urlshortener.controller;

import dev.excsi.urlshortener.dto.CreateUrlRequest;
import dev.excsi.urlshortener.dto.CreateUrlResponse;
import dev.excsi.urlshortener.dto.CountryClicksResponse;
import dev.excsi.urlshortener.dto.DailyClicksResponse;
import dev.excsi.urlshortener.dto.DeviceClicksResponse;
import dev.excsi.urlshortener.dto.TotalClicksResponse;
import dev.excsi.urlshortener.entity.ClickBucketEntity;
import dev.excsi.urlshortener.entity.CountryBucketEntity;
import dev.excsi.urlshortener.entity.DeviceBucketEntity;
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
@RequestMapping("/v1/")
public class PrivateApiController {

    private final AnalyticsService analyticsService;

    private final UrlHandlerService urlHandlerService;

    private final UserService userService;

    public PrivateApiController(AnalyticsService analyticsService, UrlHandlerService urlHandlerService, UserService userService) {
        this.analyticsService = analyticsService;
        this.urlHandlerService = urlHandlerService;
        this.userService = userService;
    }

    @PostMapping("/shorten")
    public ResponseEntity<CreateUrlResponse> create(
            @RequestBody CreateUrlRequest request,
            HttpServletRequest servletRequest,
            OAuth2AuthenticationToken authentication
    ) {
        String longUrl = validateUrl(request);
        UserEntity user = userService.getUser(authentication);
        Optional<UrlEntity> urlEntity = urlHandlerService.shortenUrl(longUrl, user);

        if (urlEntity.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not generate a unique short url");
        }

        String shortUrl = ServletUriComponentsBuilder.fromRequestUri(servletRequest)
                .scheme("https")
                .replacePath(urlEntity.get().getShortUrl())
                .replaceQuery(null)
                .build()
                .toUriString();

        return ResponseEntity.created(URI.create(shortUrl)).body(new CreateUrlResponse(shortUrl));
    }

    @GetMapping("/links/{shortUrl:[0-9a-zA-Z]{7}}/analytics")
    public List<DailyClicksResponse> getClicksForDays(
            @PathVariable String shortUrl,
            @RequestParam(name = "days", defaultValue = "30") int days,
            OAuth2AuthenticationToken authentication
    ) {
        UserEntity user = userService.getUser(authentication);
        List<ClickBucketEntity> clickBucketEntities = analyticsService.getClicksForDays(shortUrl, days, user);

        return clickBucketEntities.stream()
                .map(bucket -> new DailyClicksResponse(bucket.getId().getBucketDate(), bucket.getClicks()))
                .toList();
    }

    @GetMapping("/links/{shortUrl:[0-9a-zA-Z]{7}}/analytics/total")
    public TotalClicksResponse getTotalClicks(@PathVariable String shortUrl, OAuth2AuthenticationToken authentication) {
        UserEntity user = userService.getUser(authentication);
        return new TotalClicksResponse(analyticsService.getTotalClicks(shortUrl, user));
    }

    @GetMapping("/links/{shortUrl:[0-9a-zA-Z]{7}}/analytics/devices")
    public List<DeviceClicksResponse> getDeviceClicks(@PathVariable String shortUrl, OAuth2AuthenticationToken authentication) {
        UserEntity user = userService.getUser(authentication);
        List<DeviceBucketEntity> deviceBucketEntities = analyticsService.getDeviceClicks(shortUrl, user);

        return deviceBucketEntities.stream()
                .map(bucket -> new DeviceClicksResponse(bucket.getId().getDeviceType(), bucket.getClicks()))
                .toList();
    }

    @GetMapping("/links/{shortUrl:[0-9a-zA-Z]{7}}/analytics/countries")
    public List<CountryClicksResponse> getCountryClicks(@PathVariable String shortUrl, OAuth2AuthenticationToken authentication) {
        UserEntity user = userService.getUser(authentication);
        List<CountryBucketEntity> countryBucketEntities = analyticsService.getCountryClicks(shortUrl, user);

        return countryBucketEntities.stream()
                .map(bucket -> new CountryClicksResponse(bucket.getId().getCountryCode(), bucket.getClicks()))
                .toList();
    }

    @GetMapping("/links")
    public List<UrlEntity> getUsersUrls(OAuth2AuthenticationToken authentication) {
        UserEntity user = userService.getUser(authentication);
        return urlHandlerService.getLinksForOwner(user.getId());
    }

    private String validateUrl(CreateUrlRequest request) {
        if (request.longUrl() == null || request.longUrl().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid long url");
        }

        return request.longUrl().strip();
    }
}
