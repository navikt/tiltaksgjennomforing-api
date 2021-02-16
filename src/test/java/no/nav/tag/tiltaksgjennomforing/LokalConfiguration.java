package no.nav.tag.tiltaksgjennomforing;

import no.nav.security.oidc.test.support.spring.TokenGeneratorConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import(TokenGeneratorConfiguration.class)
@Configuration
public class LokalConfiguration {
}
