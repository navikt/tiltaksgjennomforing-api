package no.nav.tag.tiltaksgjennomforing;

import no.finn.unleash.FakeUnleash;
import no.finn.unleash.Unleash;
import no.nav.security.oidc.test.support.spring.TokenGeneratorConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@Import(TokenGeneratorConfiguration.class)
@Configuration
public class LokalConfiguration {
    @Bean
    @Primary
    public Unleash unleashMock() {
        FakeUnleash fakeUnleash = new FakeUnleash();
        fakeUnleash.enableAll(); //Enabler alle toggles pr. default. Kan endres lokalt ved behov.
        return fakeUnleash;
    }
}
