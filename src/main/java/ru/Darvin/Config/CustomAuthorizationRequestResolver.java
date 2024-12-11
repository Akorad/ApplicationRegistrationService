package ru.Darvin.Config;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import jakarta.servlet.http.HttpServletRequest;

public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {
    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        // Указываем базовый путь для OAuth2 авторизации
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        // Используем стандартный resolver для получения базового OAuth2AuthorizationRequest
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
        if (authorizationRequest != null) {
            // Меняем URL перенаправления, если это необходимо
            return OAuth2AuthorizationRequest.from(authorizationRequest)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .build();
        }
        return null;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String registrationId) {
        // Используем стандартный resolver для получения базового OAuth2AuthorizationRequest
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, registrationId);
        if (authorizationRequest != null) {
            // Меняем URL перенаправления, если это необходимо
            return OAuth2AuthorizationRequest.from(authorizationRequest)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .build();
        }
        return null;
    }
}
