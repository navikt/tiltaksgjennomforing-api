package no.nav.tag.tiltaksgjennomforing.featuretoggles;

import java.util.List;
import java.util.Map;

public interface FeatureToggleService {

    public String NY_VEILEDERTILGANG = "tag.tiltak.ny.veiledertilgang";

    Map<String, Boolean> hentFeatureToggles(List<String> features);

    Boolean isEnabled(String feature);

}
