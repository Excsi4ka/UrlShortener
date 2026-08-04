package dev.excsi.urlshortener.service;

import dev.excsi.urlshortener.entity.UrlEntity;
import dev.excsi.urlshortener.exception.UrlNotFoundException;
import dev.excsi.urlshortener.repository.UrlRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
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
            String shortUrl = shortCodeService.shortCodeFor(longUrl, attempt);
            UrlEntity existingUrl = urlRepository.findById(shortUrl).orElse(null);
            if (existingUrl != null) {
                if (existingUrl.getLongUrl().equals(longUrl)) {
                    return Optional.of(existingUrl);
                }
                continue;
            }

            try {
                return Optional.of(urlRepository.saveAndFlush(new UrlEntity(shortUrl, longUrl)));
            } catch (DataIntegrityViolationException exception) {
                UrlEntity existingUrlAfterRace = urlRepository.findById(shortUrl).orElse(null);
                if (existingUrlAfterRace == null) {
                    throw exception;
                }
                if (existingUrlAfterRace.getLongUrl().equals(longUrl)) {
                    return Optional.of(existingUrlAfterRace);
                }
            }
        }

        return Optional.empty();
    }
}
