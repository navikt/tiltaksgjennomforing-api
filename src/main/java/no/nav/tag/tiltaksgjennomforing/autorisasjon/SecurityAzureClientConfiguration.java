package no.nav.tag.tiltaksgjennomforing.autorisasjon;


import no.nav.security.token.support.client.core.ClientProperties;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;


@EnableOAuth2Client(cacheEnabled = true)
@Configuration
@Profile(value = { Miljø.PROD_FSS, Miljø.DEV_FSS })
public class SecurityAzureClientConfiguration {
    RestTemplateBuilder restTemplateBuilder;
    ClientConfigurationProperties clientConfigurationProperties;
    OAuth2AccessTokenService oAuth2AccessTokenService;

    @Bean
    public RestTemplate påVegneAvSaksbehandlerGraphRestTemplate() {
        return this.restTemplateForAzureDirective("aad-graph");
    }

    @Bean
    public RestTemplate påVegneAvSaksbehandlerProxyRestTemplate() {
        return this.restTemplateForAzureDirective("aad");
    }

    @Bean
    public RestTemplate anonymProxyRestTemplate() {
        return this.restTemplateForAzureDirective("aad-anonym");
    }

    private RestTemplate restTemplateForAzureDirective(String registrationKey) {
        final ClientProperties clientProperties = clientConfigurationProperties.getRegistration().get(registrationKey);
        return restTemplateBuilder.additionalInterceptors(bearerTokenInterceptor(clientProperties, oAuth2AccessTokenService)).build();
    }

    private ClientHttpRequestInterceptor bearerTokenInterceptor(final ClientProperties clientProperties, final OAuth2AccessTokenService oAuth2AccessTokenService) {
        return (request, body, execution) -> {
            OAuth2AccessTokenResponse response = oAuth2AccessTokenService.getAccessToken(clientProperties);
            HttpHeaders headers = request.getHeaders();
            if(response == null || body == null) {
                throw new TilgangskontrollException("Azure klient feilet med lesing av response data");
            }
            headers.setBearerAuth(response.getAccessToken());
            return execution.execute(request, body);
        };
    }
}
