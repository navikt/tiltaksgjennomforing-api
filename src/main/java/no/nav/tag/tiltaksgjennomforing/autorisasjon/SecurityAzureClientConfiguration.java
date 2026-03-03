package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.client.core.ClientProperties;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client;
import no.nav.security.token.support.client.spring.oauth2.OAuth2ClientRequestInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

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
    public RestTemplate tokenxAltinn3RestTemplate(
        RestTemplateBuilder restTemplateBuilder,
        ClientConfigurationProperties clientConfigurationProperties,
        OAuth2AccessTokenService oAuth2AccessTokenService
    ) {
        ClientProperties clientProperties = clientConfigurationProperties.getRegistration().get("tokenx-altinn-3");
        if (clientProperties == null) {
            return restTemplateBuilder.build();
        }
        ClientHttpRequestInterceptor bearerTokenInterceptor = (request, body, execution) -> {
            String accessToken = oAuth2AccessTokenService.getAccessToken(clientProperties).getAccessToken();
            if (accessToken != null) {
                request.getHeaders().setBearerAuth(accessToken);
            }
            return execution.execute(request, body);
        };
        return restTemplateBuilder
            .additionalInterceptors(bearerTokenInterceptor)
            .build();
    }
}
