package dev.excsi.urlshortener.service;

import dev.excsi.urlshortener.entity.UserEntity;
import dev.excsi.urlshortener.entity.UrlEntity;
import dev.excsi.urlshortener.exception.UrlNotFoundException;
import dev.excsi.urlshortener.repository.UrlRepository;
import dev.excsi.urlshortener.repository.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UrlHandlerService {

    private final UrlRepository urlRepository;

    private final UserRepository userRepository;

    private final EncodingService shortCodeService;

    public UrlHandlerService(UrlRepository repository, UserRepository userRepository, EncodingService shortCodeService) {
        this.urlRepository = repository;
        this.userRepository = userRepository;
        this.shortCodeService = shortCodeService;
    }

    public UrlEntity getUrlEntity(String shortUrl) {
        return urlRepository.findById(shortUrl).orElseThrow(() -> new UrlNotFoundException(shortUrl));
    }

    @Cacheable(cacheNames = "redirects", key = "#shortUrl")
    public String getLongUrl(String shortUrl) {
        return getUrlEntity(shortUrl).getLongUrl();
    }

    @Transactional
    public Optional<UrlEntity> shortenUrl(String longUrl) {
        return shortenUrl(longUrl, false, null);
    }

    @Transactional
    public Optional<UrlEntity> shortenUrlWithAnalytics(String longUrl, UserEntity owner) {
        if (owner == null || owner.getId() == null) {
            throw new IllegalArgumentException("owner must be a persisted user");
        }

        return shortenUrl(longUrl, true, owner);
    }

    public List<UrlEntity> getLinksForOwner(UserEntity owner) {
        return urlRepository.findAllByOwnerId(owner.getId());
    }

    private Optional<UrlEntity> shortenUrl(String longUrl, boolean hasAnalytics, UserEntity owner) {
        UserEntity ownerReference = owner == null ? null : userRepository.getReferenceById(owner.getId());
        String shortCodeSeed = owner == null ? longUrl : longUrl + ":owner:" + owner.getId();

        for (int attempt = 0; attempt < 20; attempt++) {
            String shortUrl = shortCodeService.shortCodeFor(shortCodeSeed, attempt);
            UrlEntity existingUrl = urlRepository.findById(shortUrl).orElse(null);
            if (existingUrl != null) {
                if (canReuse(existingUrl, longUrl, hasAnalytics, owner)) {
                    return Optional.of(existingUrl);
                }
                continue;
            }

            try {
                UrlEntity newUrl = new UrlEntity(shortUrl, longUrl, hasAnalytics);
                if (ownerReference == null) {
                    return Optional.of(urlRepository.saveAndFlush(newUrl));
                }

                ownerReference.addUrl(newUrl);
                userRepository.flush();
                return Optional.of(newUrl);
            } catch (DataIntegrityViolationException exception) {
                UrlEntity existingUrlAfterRace = urlRepository.findById(shortUrl).orElse(null);
                if (existingUrlAfterRace == null) {
                    throw exception;
                }
                if (canReuse(existingUrlAfterRace, longUrl, hasAnalytics, owner)) {
                    return Optional.of(existingUrlAfterRace);
                }
            }
        }

        return Optional.empty();
    }

    private boolean canReuse(UrlEntity existingUrl, String longUrl, boolean hasAnalytics, UserEntity owner) {
        if (!existingUrl.getLongUrl().equals(longUrl) || existingUrl.hasAnalytics() != hasAnalytics) {
            return false;
        }

        if (owner == null) {
            return urlRepository.findAnonymousUrl(existingUrl.getShortCode()).isPresent();
        }

        return urlRepository.findOwnedAnalyticsUrl(existingUrl.getShortCode(), owner.getId()).isPresent();
    }
}
