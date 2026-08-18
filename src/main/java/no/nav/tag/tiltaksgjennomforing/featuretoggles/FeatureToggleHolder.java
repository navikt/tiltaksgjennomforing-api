package no.nav.tag.tiltaksgjennomforing.featuretoggles;

import org.springframework.stereotype.Component;

@Component
public class FeatureToggleHolder {

    private static volatile FeatureToggleService instance;

    public FeatureToggleHolder(FeatureToggleService featureToggleService) {
        FeatureToggleHolder.instance = featureToggleService;
    }

    public static FeatureToggleService get() {
        if (instance == null) {
            throw new IllegalStateException("FeatureToggleHolder er ikke initialisert — Spring-konteksten er ikke startet");
        }
        return instance;
    }
}
