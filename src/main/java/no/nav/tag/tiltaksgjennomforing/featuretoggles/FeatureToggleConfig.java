package no.nav.tag.tiltaksgjennomforing.featuretoggles;

import no.finn.unleash.DefaultUnleash;
import no.finn.unleash.Unleash;
import no.finn.unleash.util.UnleashConfig;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.ByEnhetStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

import javax.servlet.http.HttpServletRequest;


@Configuration
public class FeatureToggleConfig {

    private static final String APP_NAME = "tiltaksgjennomforing-api";

    @Bean
    @ConditionalOnProperty("tiltaksgjennomforing.unleash.enabled")
    public Unleash initializeUnleash(@Value(
            "${tiltaksgjennomforing.unleash.unleash-uri}") String unleashUrl,
                                     ByEnvironmentStrategy byEnvironmentStrategy,
                                     ByEnhetStrategy byEnhetStrategy,
                                     ByOrgnummerStrategy byOrgnummerStrategy) {
        UnleashConfig config = UnleashConfig.builder()
                .appName(APP_NAME)
                .instanceId(APP_NAME + "-" + byEnvironmentStrategy.getEnvironment())
                .unleashAPI(unleashUrl)
                .build();

        return new DefaultUnleash(
                config,
                byEnvironmentStrategy,
                byEnhetStrategy,
                byOrgnummerStrategy
        );
    }

    @Bean
    @ConditionalOnProperty("tiltaksgjennomforing.unleash.mock")
    @RequestScope
    public Unleash unleashMock(@Autowired HttpServletRequest request) {
        FakeFakeUnleash fakeUnleash = new FakeFakeUnleash();
        boolean allEnabled = "enabled".equals(request.getHeader("features"));
        if (allEnabled) {
            fakeUnleash.enableAll();
        } else {
            fakeUnleash.disableAll();
        }
        return fakeUnleash;
    }
}
