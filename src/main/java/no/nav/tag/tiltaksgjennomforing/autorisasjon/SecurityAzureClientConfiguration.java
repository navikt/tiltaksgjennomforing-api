package no.nav.tag.tiltaksgjennomforing.autorisasjon;


import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client;
import no.nav.security.token.support.client.spring.oauth2.OAuth2ClientRequestInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
}
