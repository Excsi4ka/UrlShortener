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

    private final EncodingService encodingService;

    public UrlHandlerService(UrlRepository repository, UserRepository userRepository, EncodingService encodingService) {
        this.urlRepository = repository;
        this.userRepository = userRepository;
        this.encodingService = encodingService;
    }

    public UrlEntity getUrlEntity(String shortUrl) {
        return urlRepository.findById(shortUrl).orElseThrow(() -> new UrlNotFoundException(shortUrl));
    }

    @Cacheable(cacheNames = "redirects", key = "#shortUrl")
    public String getLongUrl(String shortUrl) {
        return getUrlEntity(shortUrl).getLongUrl();
    }

    @Transactional(readOnly = true)
    public List<UrlEntity> getLinksForOwner(Long ownerId) {
        return urlRepository.findAllByOwnerId(ownerId);
    }

    @Transactional
    public Optional<UrlEntity> shortenUrl(String longUrl, UserEntity owner) {
        UserEntity ownerReference = userRepository.getReferenceById(owner.getId());
        String shortCodeSeed = longUrl + ":owner:" + owner.getId();

        for (int attempt = 0; attempt < 20; attempt++) {
            String shortUrl = encodingService.shortCodeFor(shortCodeSeed, attempt);
            UrlEntity existingUrl = urlRepository.findById(shortUrl).orElse(null);
            if (existingUrl != null) {
                if (canReuse(existingUrl, longUrl, owner)) {
                    return Optional.of(existingUrl);
                }
                continue;
            }

            try {
                return Optional.of(urlRepository.saveAndFlush(new UrlEntity(shortUrl, longUrl, ownerReference)));
            } catch (DataIntegrityViolationException exception) {
                UrlEntity existingUrlAfterRace = urlRepository.findById(shortUrl).orElse(null);
                if (existingUrlAfterRace == null) {
                    throw exception;
                }
                if (canReuse(existingUrlAfterRace, longUrl, owner)) {
                    return Optional.of(existingUrlAfterRace);
                }
            }
        }

        return Optional.empty();
    }

    private boolean canReuse(UrlEntity existingUrl, String longUrl, UserEntity owner) {
        if (!existingUrl.getLongUrl().equals(longUrl)) {
            return false;
        }

        return existingUrl.getOwnerId().equals(owner.getId());
    }
}
