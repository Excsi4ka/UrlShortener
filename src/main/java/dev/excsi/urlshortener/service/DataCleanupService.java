package dev.excsi.urlshortener.service;

import dev.excsi.urlshortener.repository.ClickBucketRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
public class DataCleanupService {

    private final ClickBucketRepository clickBucketRepository;

    public DataCleanupService(ClickBucketRepository clickBucketRepository) {
        this.clickBucketRepository = clickBucketRepository;
    }

    //Every day at 00:05 AM
    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void cleanupOldEntries() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        clickBucketRepository.deleteOlderThan(today.minus(Duration.ofDays(30)));
    }
}
