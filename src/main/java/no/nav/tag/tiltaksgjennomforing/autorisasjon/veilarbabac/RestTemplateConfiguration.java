package no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {

    @Bean
    public RestTemplate stsBasicAuthRestTemplate() {
        return new RestTemplate();
    }

}
