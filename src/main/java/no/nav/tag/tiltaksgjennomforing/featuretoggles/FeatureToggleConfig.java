package no.nav.tag.tiltaksgjennomforing.featuretoggles;


import io.getunleash.DefaultUnleash;
import io.getunleash.Unleash;
import io.getunleash.util.UnleashConfig;
import jakarta.servlet.http.HttpServletRequest;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.ByEnhetStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


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
    @RequestScope
    @Profile({ Miljø.LOCAL, Miljø.DOCKER_COMPOSE, Miljø.DEV_GCP_LABS })
    public Unleash unleashMock(@Autowired HttpServletRequest request) {
        FakeFakeUnleash fakeUnleash = new FakeFakeUnleash();

        List<String> headers = Optional.ofNullable(request.getHeader("features"))
            .map(feature -> List.of(feature.split(",")))
            .orElse(Collections.emptyList());

        Optional<String> first = headers.stream().findFirst();
        if (first.map("enabled"::equals).orElse(false)) {
            fakeUnleash.enableAll();
            headers.forEach(header -> {
                if (header.startsWith("!")) {
                    fakeUnleash.disable(header.substring(1));
                }
            });
        } else if(first.map("disabled"::equals).orElse(false)) {
            fakeUnleash.disableAll();
            headers.forEach(header -> {
                if (header.startsWith("!")) {
                    fakeUnleash.enable(header.substring(1));
                }
            });
        } else {
            fakeUnleash.disableAll();
        }

        return fakeUnleash;
    }

    @Bean
    @Profile(Miljø.TEST)
    public Unleash unleashTestMock() {
        FakeFakeUnleash fakeUnleash = new FakeFakeUnleash();
        fakeUnleash.enable(FeatureToggle.SMS_TIL_MOBILNUMMER.getToggleNavn());
        return fakeUnleash;
    }
}
