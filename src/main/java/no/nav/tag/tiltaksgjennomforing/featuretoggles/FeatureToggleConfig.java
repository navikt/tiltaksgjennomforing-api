package no.nav.tag.tiltaksgjennomforing.featuretoggles;


import io.getunleash.DefaultUnleash;
import io.getunleash.Unleash;
import io.getunleash.util.UnleashConfig;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.ByEnhetStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Configuration
public class FeatureToggleConfig {

    private static final String APP_NAME = "tiltaksgjennomforing-api";

    @Bean
    @ConditionalOnProperty("tiltaksgjennomforing.unleash.enabled")
    public Unleash initializeUnleash(
            @Value("${tiltaksgjennomforing.unleash.api-uri}") String unleashUrl,
            @Value("${tiltaksgjennomforing.unleash.api-token}") String apiKey,
                                     ByEnvironmentStrategy byEnvironmentStrategy,
                                     ByEnhetStrategy byEnhetStrategy,
                                     ByOrgnummerStrategy byOrgnummerStrategy) {
        UnleashConfig config = UnleashConfig.builder()
                .appName(APP_NAME)
                .instanceId(APP_NAME + "-" + byEnvironmentStrategy.getEnvironment())
                .unleashAPI(unleashUrl)
                .apiKey(apiKey)
                .build();

        return new DefaultUnleash(
                config,
                byEnvironmentStrategy,
                byEnhetStrategy,
                byOrgnummerStrategy
        );
    }

    @Bean
    @Profile({ Miljø.LOCAL, Miljø.DOCKER_COMPOSE, Miljø.DEV_GCP_LABS })
    public Unleash unleashMock() {
        return new FakeUnleash();
    }

    @Bean
    @Profile(Miljø.TEST)
    public Unleash unleashTestMock() {
        FakeUnleash fakeUnleash = new FakeUnleash(Miljø.TEST);
        fakeUnleash.enable(FeatureToggle.SMS_TIL_MOBILNUMMER.getToggleNavn());
        return fakeUnleash;
    }
}
