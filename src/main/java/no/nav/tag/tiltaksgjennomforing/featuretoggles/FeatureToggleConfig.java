package no.nav.tag.tiltaksgjennomforing.featuretoggles;

import no.finn.unleash.DefaultUnleash;
import no.finn.unleash.Unleash;
import no.finn.unleash.util.UnleashConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Configuration
@Profile(value= {"prod", "preprod"})
public class FeatureToggleConfig {

    private final String APP_NAME = "tiltaksgjennomforing-api";
    private final ByEnvironmentStrategy byEnvironmentStrategy;

    @Value("${tiltaksgjennomforing.unleash.unleash-uri}") private String unleashUrl;
    @Value("${spring.profiles.active}") private String profile;

    @Autowired
    public FeatureToggleConfig(ByEnvironmentStrategy byEnvironmentStrategy) {
        this.byEnvironmentStrategy = byEnvironmentStrategy;
    }

    @Bean
    public Unleash initializeUnleash() {
        UnleashConfig config = UnleashConfig.builder()
                .appName(APP_NAME)
                .instanceId(APP_NAME + "-" + profile)
                .unleashAPI(unleashUrl)
                .build();

        return new DefaultUnleash(
                config,
                byEnvironmentStrategy
        );
    }
}
