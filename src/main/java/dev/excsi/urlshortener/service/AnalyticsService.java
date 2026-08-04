package dev.excsi.urlshortener.service;

import dev.excsi.urlshortener.entity.ClickBucketEntity;
import dev.excsi.urlshortener.entity.UserEntity;
import dev.excsi.urlshortener.entity.UrlEntity;
import dev.excsi.urlshortener.exception.UrlNotFoundException;
import dev.excsi.urlshortener.repository.ClickBucketRepository;
import dev.excsi.urlshortener.repository.UrlRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class AnalyticsService {

    private final ClickBucketRepository clickBucketRepository;

    private final UrlRepository urlRepository;

    public AnalyticsService(ClickBucketRepository clickBucketRepository, UrlRepository urlRepository) {
        this.clickBucketRepository = clickBucketRepository;
        this.urlRepository = urlRepository;
    }

    @Transactional
    public void recordClick(String shortUrl) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        int updatedUrls = urlRepository.incrementTotalClickCount(shortUrl, 1);

        if (updatedUrls > 0) {
            clickBucketRepository.incrementClicks(shortUrl, today, 1);
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

    public int getTotalClicks(String shortUrl) {
        return urlRepository.findById(shortUrl).orElseThrow(() -> new UrlNotFoundException(shortUrl)).getTotalClicks();
    }

    public int getTotalClicks(String shortUrl, UserEntity owner) {
        return requireOwnedAnalyticsUrl(shortUrl, owner).getTotalClicks();
    }

    private UrlEntity requireOwnedAnalyticsUrl(String shortUrl, UserEntity owner) {
        return urlRepository.findOwnedAnalyticsUrl(shortUrl, owner.getId())
                .orElseThrow(() -> new UrlNotFoundException(shortUrl));
    }
}
