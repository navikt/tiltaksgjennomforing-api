package no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;

@RequiredArgsConstructor
@Component
@Profile("!prod")
public class TilgangskontrollAvslattNotProd implements TilgangskontrollAvslatt {

    private static final String TAG_TILTAK_TILGANGSKONTROLL_AVSLATT = "tag.tiltak.tilgangskontroll.avslatt";

    private final FeatureToggleService featureToggleService;
    
    @Override
    public boolean tilgangskontrollAvslatt() {
        return featureToggleService.isEnabled(TAG_TILTAK_TILGANGSKONTROLL_AVSLATT);
    }

}
