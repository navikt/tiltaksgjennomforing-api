package no.nav.tag.tiltaksgjennomforing.featuretoggles;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import static java.lang.Boolean.TRUE;
import static java.util.Optional.ofNullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Profile(value= {"dev", "heroku"})
public class FeatureToggleServiceMock implements FeatureToggleService {

    private static final Map<String, Boolean> TOGGLES = new HashMap<String, Boolean>() {{
            put("test1", true);
            put("test2", false);
        }};

    public Map<String, Boolean> hentFeatureToggles(List<String> features) {

        return features.stream().collect(Collectors.toMap(
                feature -> feature,
                feature -> isEnabled(feature)
        ));
    }

    public Boolean isEnabled(String feature) {
        return ofNullable(TOGGLES.get(feature)).orElseGet(() -> TRUE);
    }

}
