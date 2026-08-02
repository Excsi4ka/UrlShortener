package dev.excsi.urlshortener.service;

import dev.excsi.urlshortener.entity.UrlEntity;
import dev.excsi.urlshortener.exception.UrlNotFoundException;
import dev.excsi.urlshortener.repository.UrlRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UrlHandlerService {

    private final UrlRepository urlRepository;

    private final EncodingService shortCodeService;

    public UrlHandlerService(UrlRepository repository, EncodingService shortCodeService) {
        this.urlRepository = repository;
        this.shortCodeService = shortCodeService;
    }

    public UrlEntity getUrlEntity(String shortUrl) {
        return urlRepository.findById(shortUrl).orElseThrow(() -> new UrlNotFoundException(shortUrl));
    }

    @Cacheable(cacheNames = "redirects", key = "#shortUrl")
    public String getLongUrl(String shortUrl) {
        return getUrlEntity(shortUrl).getLongUrl();
    }

    public Optional<UrlEntity> shortenUrl(String longUrl) {
        for (int attempt = 0; attempt < 20; attempt++) {
            String shortCode = shortCodeService.shortCodeFor(longUrl, attempt);
            UrlEntity existingUrl = urlRepository.findById(shortCode).orElse(null);
            if (existingUrl != null) {
                if (existingUrl.getLongUrl().equals(longUrl)) {
                    return Optional.of(existingUrl);
                }
                continue;
            }
            return Optional.of(urlRepository.save(new UrlEntity(shortCode, longUrl)));
        }

        return Optional.empty();
    }
}
