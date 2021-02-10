package no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {

    @Bean
    @Primary
    public RestTemplate stsBasicAuthRestTemplate() {
        return new RestTemplate();
    }

}
