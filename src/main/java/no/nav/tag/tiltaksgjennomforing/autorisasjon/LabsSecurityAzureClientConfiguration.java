package no.nav.tag.tiltaksgjennomforing.autorisasjon;


import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;


@Configuration
@Profile(value = {Miljø.LABS_GCP})
@Slf4j
public class LabsSecurityAzureClientConfiguration {
    @Bean("notifikasjonerRestTemplate")
    public RestTemplate anonymProxyRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }

    @Bean("veilarbarenaRestTemplate")
    public RestTemplate anonymProxyRestTemplateVeilabArena(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }

}
