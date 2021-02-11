package no.nav.tag.tiltaksgjennomforing;

import no.nav.security.oidc.test.support.spring.TokenGeneratorConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestTemplate;

@Import(TokenGeneratorConfiguration.class)
@Configuration
public class LokalConfiguration {

}
