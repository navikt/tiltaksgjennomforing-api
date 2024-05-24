package no.nav.tag.tiltaksgjennomforing.autorisasjon;


import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.client.core.ClientProperties;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;
import no.nav.security.token.support.client.spring.oauth2.ClientConfigurationPropertiesMatcher;
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client;
import no.nav.security.token.support.client.spring.oauth2.OAuth2ClientRequestInterceptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;

@EnableOAuth2Client(cacheEnabled = true)
@Configuration
@Slf4j
public class SecurityAzureClientConfiguration {
    
    @Bean
    public RestTemplate azureRestTemplate(
        RestTemplateBuilder restTemplateBuilder,
        OAuth2ClientRequestInterceptor oAuth2ClientRequestInterceptor
    ) {
        return restTemplateBuilder
            .interceptors(oAuth2ClientRequestInterceptor)
            .build();
    }

    @Bean
    public OAuth2ClientRequestInterceptor oAuth2ClientRequestInterceptor(
        ClientConfigurationProperties properties,
        OAuth2AccessTokenService service,
        ClientConfigurationPropertiesMatcher matcher
    ) {
        return new OAuth2ClientRequestInterceptor(properties, service, matcher);
    }

    @Bean
    public ClientConfigurationPropertiesMatcher clientConfigurationPropertiesMatcher() {
        return new ClientConfigurationPropertiesMatcher() {
            @Nullable
            @Override
            public ClientProperties findProperties(
                @NotNull ClientConfigurationProperties properties,
                @NotNull String s
            ) {
                return findProperties(properties, URI.create(s));
            }

            @Nullable
            @Override
            public ClientProperties findProperties(
                @NotNull ClientConfigurationProperties properties,
                @NotNull URI uri
            ) {
                String registration = Arrays.stream(uri.getHost().split("\\.")).findFirst().orElse("");

                return switch (registration) {
                    case "sokos-kontoregister-q2" -> properties.getRegistration().get("sokos-kontoregister");
                    default -> properties.getRegistration().get(registration);
                };
            }
        };
    }
}
