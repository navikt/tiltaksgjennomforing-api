package no.nav.tag.tiltaksgjennomforing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
class TiltaksgjennomforingConfiguration {
    @Bean
    public RestTemplate noAuthRestTemplate() {
        return new RestTemplate();
    }
}
