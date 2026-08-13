package dev.excsi.urlshortener.service;

import dev.excsi.urlshortener.entity.UserEntity;
import dev.excsi.urlshortener.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
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
    public void createOrUpdateUser(OAuth2AuthenticationToken authentication) {
        createOrUpdateUser(authentication.getAuthorizedClientRegistrationId(), authentication.getPrincipal());
    }

    private void createOrUpdateUser(String provider, OAuth2User oauthUser) {
        String providerUserId = providerUserId(oauthUser);
        String email = attributeAsString(oauthUser, "email");
        String displayName = firstPresentAttribute(oauthUser, "name", "login");
        String pictureUrl = firstPresentAttribute(oauthUser, "picture", "avatar_url");

        userRepository.findByProviderAndProviderUserId(provider, providerUserId)
                .map(existingUser -> {
                    existingUser.updateProfile(email, displayName, pictureUrl);
                    return existingUser;
                })
                .orElseGet(() -> userRepository.save(
                        new UserEntity(provider, providerUserId, email, displayName, pictureUrl)
                ));
    }

    @Transactional(readOnly = true)
    public UserEntity getUser(OAuth2AuthenticationToken authentication) {
        String provider = authentication.getAuthorizedClientRegistrationId();
        String providerUserId = providerUserId(authentication.getPrincipal());

        return userRepository.findByProviderAndProviderUserId(provider, providerUserId)
                .orElseThrow(() -> new UsernameNotFoundException("User has not been provisioned for provider " + provider));
    }

    private String providerUserId(OAuth2User oauthUser) {
        String providerUserId = oauthUser instanceof OidcUser oidcUser
                ? oidcUser.getSubject()
                : firstPresentAttribute(oauthUser, "sub", "id");

        if (providerUserId == null || providerUserId.isBlank()) {
            providerUserId = oauthUser.getName();
        }

        return providerUserId;
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
