package dev.excsi.urlshortener.controller;

import dev.excsi.urlshortener.dto.AuthUserResponse;
import dev.excsi.urlshortener.dto.CsrfTokenResponse;
import dev.excsi.urlshortener.entity.UserEntity;
import dev.excsi.urlshortener.service.UserService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/v1/auth/me")
    public AuthUserResponse me(OAuth2AuthenticationToken authentication) {
        UserEntity user = userService.getUser(authentication);
        return new AuthUserResponse(
                user.getId(),
                user.getProvider(),
                user.getEmail(),
                user.getDisplayName(),
                user.getPictureUrl()
        );
    }

    @GetMapping("/v1/auth/csrf")
    public CsrfTokenResponse csrf(CsrfToken csrfToken) {
        return new CsrfTokenResponse(csrfToken.getToken());
    }
}
