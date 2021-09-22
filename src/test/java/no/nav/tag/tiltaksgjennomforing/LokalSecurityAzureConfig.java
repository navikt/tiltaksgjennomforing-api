package no.nav.tag.tiltaksgjennomforing;

import lombok.AllArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@AllArgsConstructor
public class LokalSecurityAzureConfig {

    private final RestTemplateBuilder restTemplateBuilder;

    @Bean
    public RestTemplate påVegneAvSaksbehandlerGraphRestTemplate() {
        return restTemplateBuilder.build();
    }

    @Bean
    public RestTemplate påVegneAvSaksbehandlerProxyRestTemplate(){
        return restTemplateBuilder.build();
    }

    @Bean
    public RestTemplate påVegneAvArbeidsgiverAltinnRestTemplate(){
        return restTemplateBuilder.build();
    }

    @Bean
    public RestTemplate anonymProxyRestTemplate(){
        return restTemplateBuilder.build();
    }
}
