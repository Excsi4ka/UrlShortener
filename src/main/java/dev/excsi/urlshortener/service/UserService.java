package dev.excsi.urlshortener.service;

import dev.excsi.urlshortener.entity.UserEntity;
import dev.excsi.urlshortener.repository.UserRepository;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserEntity getOrCreateUser(OAuth2AuthenticationToken authentication) {
        return upsertOAuthUser(authentication.getAuthorizedClientRegistrationId(), authentication.getPrincipal());
    }

    @Transactional
    public UserEntity upsertOAuthUser(String provider, OAuth2User oauthUser) {
        String providerUserId = firstPresentAttribute(oauthUser, "sub", "id");
        if (providerUserId == null || providerUserId.isBlank()) {
            providerUserId = oauthUser.getName();
        }

        String email = attributeAsString(oauthUser, "email");
        String displayName = firstPresentAttribute(oauthUser, "name", "login");
        String pictureUrl = firstPresentAttribute(oauthUser, "picture", "avatar_url");

        String finalProviderUserId = providerUserId;
        return userRepository.findByProviderAndProviderUserId(provider, finalProviderUserId)
                .map(existingUser -> {
                    existingUser.updateProfile(email, displayName, pictureUrl);
                    return existingUser;
                })
                .orElseGet(() -> userRepository.save(
                        new UserEntity(provider, finalProviderUserId, email, displayName, pictureUrl)
                ));
    }

    private String firstPresentAttribute(OAuth2User oauthUser, String... names) {
        for (String name : names) {
            String value = attributeAsString(oauthUser, name);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String attributeAsString(OAuth2User oauthUser, String name) {
        Object value = oauthUser.getAttribute(name);
        return value == null ? null : value.toString();
    }
}
