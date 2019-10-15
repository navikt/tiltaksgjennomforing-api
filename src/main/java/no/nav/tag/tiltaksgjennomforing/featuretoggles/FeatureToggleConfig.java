package no.nav.tag.tiltaksgjennomforing.featuretoggles;

import no.finn.unleash.DefaultUnleash;
import no.finn.unleash.Unleash;
import no.finn.unleash.util.UnleashConfig;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.ByEnhetStrategy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Configuration
@Profile(value= {"prod", "preprod"})
public class FeatureToggleConfig {

    private static final String APP_NAME = "tiltaksgjennomforing-api";

    @Bean
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
}
