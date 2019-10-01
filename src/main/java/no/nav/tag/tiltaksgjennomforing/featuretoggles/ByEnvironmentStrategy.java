package no.nav.tag.tiltaksgjennomforing.featuretoggles;

import no.finn.unleash.strategy.Strategy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static java.util.Arrays.asList;
import static no.nav.tag.tiltaksgjennomforing.infrastruktur.SjekkAktiveProfilerInitializer.MILJOER;

import java.util.Map;
import java.util.Optional;

@Component
public class ByEnvironmentStrategy implements Strategy {

    private final String environment;

    public ByEnvironmentStrategy(
            @Value("${spring.profiles.active}") String environmentList
    ) {
        this.environment = asList(environmentList.split(",")).stream().filter(a -> MILJOER.contains(a)).findFirst().orElse("dev");;
    }

    @Override
    public String getName() {
        return "byEnvironment";
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters) {
        return Optional.ofNullable(parameters)
                .map(map -> map.get("miljø"))
                .map(env -> asList(env.split(",")).contains(environment))
                .orElse(false);
    }
    
    String getEnvironment() {
        return environment;
    }
}
