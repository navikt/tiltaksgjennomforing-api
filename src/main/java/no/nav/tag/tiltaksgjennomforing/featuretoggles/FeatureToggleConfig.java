package no.nav.tag.tiltaksgjennomforing.featuretoggles;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import no.finn.unleash.DefaultUnleash;
import no.finn.unleash.FakeUnleash;
import no.finn.unleash.Unleash;
import no.finn.unleash.util.UnleashConfig;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.ByEnhetStrategy;


@Configuration
public class FeatureToggleConfig {

    private static final String APP_NAME = "tiltaksgjennomforing-api";

    @Bean
    @Profile(value= {"prod", "preprod"})
    public Unleash initializeUnleash(@Value(
            "${tiltaksgjennomforing.unleash.unleash-uri}") String unleashUrl, 
            ByEnvironmentStrategy byEnvironmentStrategy,
            ByEnhetStrategy byEnhetStrategy) {
        UnleashConfig config = UnleashConfig.builder()
                .appName(APP_NAME)
                .instanceId(APP_NAME + "-" + byEnvironmentStrategy.getEnvironment())
                .unleashAPI(unleashUrl)
                .build();

        return new DefaultUnleash(
                config,
                byEnvironmentStrategy,
                byEnhetStrategy
        );
    }
    
    @Bean
    @Profile(value= {"dev", "heroku"})
    public Unleash unleashMock() {
        FakeUnleash fakeUnleash = new FakeUnleash();
        fakeUnleash.enableAll(); //Enabler alle toggles pr. default. Kan endres lokalt ved behov.
        return fakeUnleash;
    }
}
