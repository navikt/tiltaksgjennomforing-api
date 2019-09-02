package no.nav.tag.tiltaksgjennomforing.featuretoggles;

import no.finn.unleash.Unleash;
import no.finn.unleash.UnleashContext;
import no.nav.tag.tiltaksgjennomforing.integrasjon.InnloggingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Profile(value= {"prod", "preprod"})
public class FeatureToggleServiceImpl implements FeatureToggleService {

    private final Unleash unleash;
    private final InnloggingService innloggingService;

    @Autowired
    public FeatureToggleServiceImpl(Unleash unleash, InnloggingService innloggingService) {
        this.unleash = unleash;
        this.innloggingService = innloggingService;
        
    }

    public Map<String, Boolean> hentFeatureToggles(List<String> features) {

        return features.stream().collect(Collectors.toMap(
                feature -> feature,
                feature -> isEnabled(feature)
        ));
    }

    public Boolean isEnabled(String feature) {
        return unleash.isEnabled(feature, contextMedInnloggetBruker());
    }

    private UnleashContext contextMedInnloggetBruker() {
        String bruker = null;
        try {
            bruker  = innloggingService.hentInnloggetBruker().getIdentifikator().asString();
        } catch (Exception e) {
        }
        
        UnleashContext unleashContext = UnleashContext.builder().userId(bruker).build();
        return unleashContext;
    }
}
