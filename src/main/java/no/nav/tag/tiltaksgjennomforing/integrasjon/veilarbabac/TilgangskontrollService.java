package no.nav.tag.tiltaksgjennomforing.integrasjon.veilarbabac;

import static no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService.NY_VEILEDERTILGANG;

import org.springframework.stereotype.Service;

import no.nav.tag.tiltaksgjennomforing.domene.Fnr;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetNavAnsatt;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;

@Service
public class TilgangskontrollService {
    private final VeilarbabacClient veilarbabacClient;
    private final FeatureToggleService featureToggleService;

    public TilgangskontrollService(VeilarbabacClient veilarbabacClient, FeatureToggleService featureToggleService) {
        this.veilarbabacClient = veilarbabacClient;
        this.featureToggleService = featureToggleService;
    }

    public void sjekkLesetilgangTilKandidat(InnloggetNavAnsatt innloggetNavAnsatt, Fnr fnr) {
        sjekkTilgang(innloggetNavAnsatt, fnr, TilgangskontrollAction.read);
    }

    public void sjekkSkrivetilgangTilKandidat(InnloggetNavAnsatt innloggetNavAnsatt, Fnr fnr) {
        sjekkTilgang(innloggetNavAnsatt, fnr, TilgangskontrollAction.update);
    }

    private void sjekkTilgang(InnloggetNavAnsatt innloggetNavAnsatt, Fnr fnr, TilgangskontrollAction action) {
        if (!hentTilgang(innloggetNavAnsatt, fnr, action)) {
            throw new TilgangskontrollException("Veileder har ikke følgende tilgang for kandidat: " + action);
        }
    }

    private boolean hentTilgang(InnloggetNavAnsatt innloggetNavAnsatt, Fnr fnr, TilgangskontrollAction action) {
        return !featureToggleService.isEnabled(NY_VEILEDERTILGANG) || veilarbabacClient.sjekkTilgang(
                innloggetNavAnsatt,
                fnr.asString(),
                action
        );
    }

}