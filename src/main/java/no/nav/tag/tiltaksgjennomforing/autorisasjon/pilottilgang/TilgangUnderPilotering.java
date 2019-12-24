package no.nav.tag.tiltaksgjennomforing.autorisasjon.pilottilgang;

import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;

import org.springframework.stereotype.Component;

@Data
@Component
public class TilgangUnderPilotering {

    public static final String TAG_TILTAK_PILOTTILGANG_IDENT = "tag.tiltak.pilottilgang.ident";
    public static final String TAG_TILTAK_PILOTTILGANG_KONTOR = "tag.tiltak.pilottilgang.kontor";

    private final FeatureToggleService featureToggleService;

    public void sjekkTilgang(NavIdent ident) {
        if(!pilotTilgangOk()) {
            throw new TilgangskontrollException("Ident " + ident.asString() + " er ikke lagt til i lista over brukere med tilgang.");
        }
    }

    private boolean pilotTilgangOk() {
        return featureToggleService.isEnabled(TAG_TILTAK_PILOTTILGANG_IDENT) || featureToggleService.isEnabled(TAG_TILTAK_PILOTTILGANG_KONTOR);
    }

}
