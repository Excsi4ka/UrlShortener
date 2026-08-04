package dev.excsi.urlshortener.service;

import dev.excsi.urlshortener.entity.ClickBucketEntity;
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
        clickBucketRepository.incrementClicks(shortUrl, today, 1);
        urlRepository.incrementTotalClickCount(shortUrl, 1);
    }

    public List<ClickBucketEntity> getClicksForDays(String shortUrl, int dayOffset) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        return clickBucketRepository.getBucketsSince(shortUrl, today.minus(Period.ofDays(dayOffset - 1)));
    }

    public int getTotalClicks(String shortUrl) {
        return urlRepository.findById(shortUrl).orElseThrow(() -> new UrlNotFoundException(shortUrl)).getTotalClicks();
    }
}
