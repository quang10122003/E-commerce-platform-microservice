package com.example.auth_service.infrastructure.sercurity;

import com.example.auth_service.adapter.in.oauth2.OAuth2LoginFailureHandler;
import com.example.auth_service.adapter.in.oauth2.OAuth2LoginSuccessHandler;
import com.example.auth_service.application.port.in.OAuthLoginUseCase;
import com.example.auth_service.application.strategy.oauth.GoogleOAuthLoginHandler;
import com.example.auth_service.infrastructure.config.OAuth2Properties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

@Configuration
@EnableConfigurationProperties(OAuth2Properties.class)
public class OAuth2ClientConfig {

    @Bean
    ClientRegistrationRepository clientRegistrationRepository(OAuth2Properties oauth2Properties) {
        OAuth2Properties.Google google = oauth2Properties.google();
        return new InMemoryClientRegistrationRepository(
                CommonOAuth2Provider.GOOGLE.getBuilder(GoogleOAuthLoginHandler.REGISTRATION_ID)
                        .clientId(google.clientId())
                        .clientSecret(google.clientSecret())
                        .redirectUri(google.redirectUri())
                        .build());
    }

    @Bean
    OAuth2LoginSuccessHandler oauth2LoginSuccessHandler(OAuthLoginUseCase oauthLoginUseCase,
            OAuth2Properties oauth2Properties) {
        return new OAuth2LoginSuccessHandler(oauthLoginUseCase, oauth2Properties.frontendCallbackUrl());
    }

    @Bean
    OAuth2LoginFailureHandler oauth2LoginFailureHandler(OAuth2Properties oauth2Properties) {
        return new OAuth2LoginFailureHandler(oauth2Properties.frontendCallbackUrl());
    }
}
