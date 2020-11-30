package no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.exceptions.IkkeTilgangTilDeltakerException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TilgangskontrollService {

    private final VeilarbabacClient veilarbabacClient;

    public boolean harSkrivetilgangTilKandidat(NavIdent navIdent, Fnr fnr) {
        return hentTilgang(navIdent, fnr, TilgangskontrollAction.update);
    }

    public void sjekkSkrivetilgangTilKandidat(NavIdent navIdent, Fnr fnr) {
        sjekkTilgang(navIdent, fnr, TilgangskontrollAction.update);
    }

    private void sjekkTilgang(NavIdent navIdent, Fnr fnr, TilgangskontrollAction action) {
        if (!hentTilgang(navIdent, fnr, action)) {
            throw new IkkeTilgangTilDeltakerException();
        }
    }

    private boolean hentTilgang(NavIdent navIdent, Fnr fnr, TilgangskontrollAction action) {
        return veilarbabacClient.sjekkTilgang(
                fnr.asString(),
                action, navIdent
        );
    }

}