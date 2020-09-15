package no.nav.tag.tiltaksgjennomforing.featuretoggles;

import no.finn.unleash.strategy.Strategy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;

@Component
public class ByEnvironmentStrategy implements Strategy {

    private final String environment;

    public ByEnvironmentStrategy() {
        this.environment = Optional.ofNullable(System.getenv("NAIS_CLUSTER_NAME")).orElse("local");
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
