package no.nav.tag.tiltaksgjennomforing.featuretoggles;

import no.finn.unleash.strategy.Strategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

@Component
public class ByEnvironmentStrategy implements Strategy {
    private final String environment;

    public ByEnvironmentStrategy(
            @Value("${spring.profiles.active}") String environment
    ) {
        this.environment = environment;
    }

    @Override
    public String getName() {
        return "byEnvironment";
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters) {
        if (parameters == null) {
            return false;
        }

        String miljøParameter = parameters.get("miljø");
        if (miljøParameter == null) {
            return false;
        }

        String[] miljøer = miljøParameter.split(",");
        return Arrays.asList(miljøer).contains(environment);
    }
}
