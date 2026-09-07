package no.nav.tag.tiltaksgjennomforing.featuretoggles;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class FeatureToggleConverter implements Converter<String, FeatureToggle> {

    private static final Map<String, FeatureToggle> OPPSLAG = new HashMap<>();

    static {
        for (FeatureToggle featureToggle : FeatureToggle.values()) {
            OPPSLAG.put(featureToggle.getToggleNavn(), featureToggle);
        }
    }

    @Override
    public FeatureToggle convert(String source) {
        return Optional.ofNullable(OPPSLAG.get(source))
                .orElseThrow(() -> new IllegalArgumentException("Ukjent feature toggle: " + source));
    }
}
