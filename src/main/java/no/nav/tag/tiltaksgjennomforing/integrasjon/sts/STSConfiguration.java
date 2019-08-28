package no.nav.tag.tiltaksgjennomforing.integrasjon.sts;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class STSConfiguration {

    @Value("${TILTAKSGJENNOMFORING_SERVICEUSER_USERNAME}")
    private String brukernavn;

    @Value("${TILTAKSGJENNOMFORING_SERVICEUSER_PASSWORD}")
    private String passord;

    @Bean
    public RestTemplate stsBasicAuthRestTemplate() {
        return new RestTemplateBuilder()
                .basicAuthentication(brukernavn, passord)
                .build();
    }

}
