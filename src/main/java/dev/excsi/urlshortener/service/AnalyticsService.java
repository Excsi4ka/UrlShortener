package dev.excsi.urlshortener.service;

import dev.excsi.urlshortener.entity.ClickBucketEntity;
import dev.excsi.urlshortener.entity.CountryBucketEntity;
import dev.excsi.urlshortener.entity.DeviceBucketEntity;
import dev.excsi.urlshortener.entity.DeviceType;
import dev.excsi.urlshortener.entity.UserEntity;
import dev.excsi.urlshortener.entity.UrlEntity;
import dev.excsi.urlshortener.exception.UrlNotFoundException;
import dev.excsi.urlshortener.repository.ClickBucketRepository;
import dev.excsi.urlshortener.repository.CountryBucketRepository;
import dev.excsi.urlshortener.repository.DeviceBucketRepository;
import dev.excsi.urlshortener.repository.UrlRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;

@Service
public class AnalyticsService {

    private static final String UNKNOWN_COUNTRY_CODE = "ZZ";

    private final ClickBucketRepository clickBucketRepository;

    private final DeviceBucketRepository deviceBucketRepository;

    private final CountryBucketRepository countryBucketRepository;

    private final UrlRepository urlRepository;

    public AnalyticsService(ClickBucketRepository clickBucketRepository, DeviceBucketRepository deviceBucketRepository, CountryBucketRepository countryBucketRepository, UrlRepository urlRepository) {
        this.clickBucketRepository = clickBucketRepository;
        this.deviceBucketRepository = deviceBucketRepository;
        this.countryBucketRepository = countryBucketRepository;
        this.urlRepository = urlRepository;
    }

    @Transactional
    public void recordClick(String shortUrl, String country, String userAgent, String userAgentMobile, String userAgentPlatform) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        int updatedUrls = urlRepository.incrementTotalClickCount(shortUrl, 1);

        if (updatedUrls > 0) {
            clickBucketRepository.incrementClicks(shortUrl, today, 1);
            deviceBucketRepository.incrementClicks(shortUrl, deviceType(userAgent, userAgentMobile, userAgentPlatform).name(), 1);
            countryBucketRepository.incrementClicks(shortUrl, countryCode(country), 1);
        }
    }

    public List<ClickBucketEntity> getClicksForDays(String shortUrl, int dayOffset) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        return clickBucketRepository.getBucketsSince(shortUrl, today.minus(Period.ofDays(dayOffset - 1)));
    }

    public List<ClickBucketEntity> getClicksForDays(String shortUrl, int dayOffset, UserEntity owner) {
        requireOwnedAnalyticsUrl(shortUrl, owner);
        return getClicksForDays(shortUrl, dayOffset);
    }

    public List<DeviceBucketEntity> getDeviceClicks(String shortUrl, UserEntity owner) {
        requireOwnedAnalyticsUrl(shortUrl, owner);
        return deviceBucketRepository.getBuckets(shortUrl);
    }

    public List<CountryBucketEntity> getCountryClicks(String shortUrl, UserEntity owner) {
        requireOwnedAnalyticsUrl(shortUrl, owner);
        return countryBucketRepository.getBuckets(shortUrl);
    }

    public int getTotalClicks(String shortUrl, UserEntity owner) {
        return requireOwnedAnalyticsUrl(shortUrl, owner).getTotalClicks();
    }

    private UrlEntity requireOwnedAnalyticsUrl(String shortUrl, UserEntity owner) {
        return urlRepository.findOwnedAnalyticsUrl(shortUrl, owner.getId())
                .orElseThrow(() -> new UrlNotFoundException(shortUrl));
    }

    private DeviceType deviceType(String userAgent, String userAgentMobile, String userAgentPlatform) {
        String platform = normalizedHeaderValue(userAgentPlatform);
        String ua = userAgent == null ? "" : userAgent.toLowerCase(Locale.ROOT);

        if ("android".equals(platform) && "?0".equals(userAgentMobile)) {
            return DeviceType.TABLET;
        }
        if ("?1".equals(userAgentMobile)) {
            return DeviceType.MOBILE;
        }

        if (ua.contains("ipad") || ua.contains("tablet")) {
            return DeviceType.TABLET;
        }
        if (ua.contains("android")) {
            return ua.contains("mobile") ? DeviceType.MOBILE : DeviceType.TABLET;
        }
        if (ua.contains("mobi") || ua.contains("iphone") || ua.contains("ipod")) {
            return DeviceType.MOBILE;
        }
        if ("?0".equals(userAgentMobile)
                || ua.contains("windows")
                || ua.contains("macintosh")
                || ua.contains("x11")
                || ua.contains("cros")) {
            return DeviceType.DESKTOP;
        }

        return DeviceType.UNKNOWN;
    }

    private String countryCode(String country) {
        if (country == null || country.isBlank()) {
            return UNKNOWN_COUNTRY_CODE;
        }

        String normalizedCountry = country.strip().toUpperCase(Locale.ROOT);
        if (normalizedCountry.matches("[A-Z0-9]{2}")) {
            return normalizedCountry;
        }

        return UNKNOWN_COUNTRY_CODE;
    }

    private String normalizedHeaderValue(String value) {
        if (value == null) {
            return "";
        }

        return value.strip().replace("\"", "").toLowerCase(Locale.ROOT);
    }
}
