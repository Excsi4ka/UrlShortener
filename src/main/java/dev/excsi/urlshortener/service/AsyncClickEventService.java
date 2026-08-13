package dev.excsi.urlshortener.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncClickEventService {

    private final AnalyticsService analyticsService;

    public AsyncClickEventService(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Async("analyticsExecutor")
    public void recordClick(String shortUrl, String country, String userAgent, String userAgentMobile, String userAgentPlatform) {
        analyticsService.recordClick(shortUrl, country, userAgent, userAgentMobile, userAgentPlatform);
    }
}
