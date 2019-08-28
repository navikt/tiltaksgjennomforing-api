package no.nav.tag.tiltaksgjennomforing.integrasjon.veilarbabac;

import org.springframework.stereotype.Service;

import no.nav.tag.tiltaksgjennomforing.domene.Fnr;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetNavAnsatt;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.integrasjon.TokenUtils;

@Service
public class TilgangskontrollService {
    private final TokenUtils tokenUtils;
    private final VeilarbabacClient  veilarbabacClient;

    public TilgangskontrollService(TokenUtils tokenUtils, VeilarbabacClient veilarbabacClient) {
        this.tokenUtils = tokenUtils;
        this.veilarbabacClient = veilarbabacClient;
    }

    public void sjekkLesetilgangTilKandidat(Fnr fnr) {
        sjekkTilgang(fnr, TilgangskontrollAction.read);
    }

    public void sjekkSkrivetilgangTilKandidat(Fnr fnr) {
        sjekkTilgang(fnr, TilgangskontrollAction.update);
    }

    private void sjekkTilgang(Fnr fnr, TilgangskontrollAction action) {
        if (!hentTilgang(fnr, action)) {
            throw new TilgangskontrollException("Veileder har ikke følgende tilgang for kandidat: " + action);
        }
    }

    private boolean hentTilgang(Fnr fnr, TilgangskontrollAction action) {
        return veilarbabacClient.sjekkTilgang(
                hentInnloggetVeileder(),
                fnr.asString(),
                action
        );
    }

    public InnloggetNavAnsatt hentInnloggetVeileder() {
        return tokenUtils.hentInnloggetNavAnsatt();
    }
}