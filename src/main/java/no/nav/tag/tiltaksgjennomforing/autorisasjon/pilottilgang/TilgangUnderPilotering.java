package no.nav.tag.tiltaksgjennomforing.autorisasjon.pilottilgang;

import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;

import org.springframework.stereotype.Component;

import static java.util.Collections.disjoint;


@Data
@Component
public class TilgangUnderPilotering {

    static final String TAG_TILTAK_PILOTTILGANG = "tag.tiltak.pilottilgang";
    static final String TAG_TILTAK_BRUK_UNLEASH_FOR_PILOTTILGANG = "tag.tiltak.bruk.unleash.for.pilottilgang";

    private final PilotProperties pilotProperties;
    private final AxsysService axsysService;
    private final FeatureToggleService featureToggleService;

    public void sjekkTilgang(NavIdent ident) {
        boolean tilgangOk = featureToggleService.isEnabled(TAG_TILTAK_BRUK_UNLEASH_FOR_PILOTTILGANG) ? sjekkPilotTilgangMedUnleash() : sjekkPilottilgangMedVault(ident);
        if(!tilgangOk) {
            throw new TilgangskontrollException("Ident " + ident.asString() + " er ikke lagt til i lista over brukere med tilgang.");
        }
    }

    private boolean sjekkPilotTilgangMedUnleash() {
        return featureToggleService.isEnabled(TAG_TILTAK_PILOTTILGANG);
    }

    private boolean sjekkPilottilgangMedVault(NavIdent ident) {
        if (pilotProperties.isEnabled() && !pilotProperties.getIdenter().contains(ident)) {
            return axsysService.hentEnheterVeilederHarTilgangTil(ident).map(enheter -> !disjoint(pilotProperties.getEnheter(), enheter)).orElse(false);
        }
        return true;
    }

}
