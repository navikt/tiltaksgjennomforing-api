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

    public static final String TAG_TILTAK_PILOTTILGANG_IDENT = "tag.tiltak.pilottilgang.ident";
    public static final String TAG_TILTAK_PILOTTILGANG_KONTOR = "tag.tiltak.pilottilgang.kontor";
    public static final String TAG_TILTAK_BRUK_UNLEASH_FOR_PILOTTILGANG = "tag.tiltak.bruk.unleash.for.pilottilgang";

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
        return featureToggleService.isEnabled(TAG_TILTAK_PILOTTILGANG_IDENT) || featureToggleService.isEnabled(TAG_TILTAK_PILOTTILGANG_KONTOR);
    }

    private boolean sjekkPilottilgangMedVault(NavIdent ident) {
        if (pilotProperties.isEnabled() && !pilotProperties.getIdenter().contains(ident)) {
            try {
                return !disjoint(pilotProperties.getEnheter(), axsysService.hentEnheterVeilederHarTilgangTil(ident));
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

}
