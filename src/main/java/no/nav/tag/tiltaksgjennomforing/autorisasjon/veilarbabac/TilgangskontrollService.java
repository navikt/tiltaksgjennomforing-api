package no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetNavAnsatt;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;

@Service
@RequiredArgsConstructor
public class TilgangskontrollService {

    private final VeilarbabacClient veilarbabacClient;

    public boolean harLesetilgangTilKandidat(InnloggetNavAnsatt innloggetNavAnsatt, Fnr fnr) {
        return hentTilgang(innloggetNavAnsatt, fnr, TilgangskontrollAction.read);
    }

    public boolean harSkrivetilgangTilKandidat(InnloggetNavAnsatt innloggetNavAnsatt, Fnr fnr) {
        return hentTilgang(innloggetNavAnsatt, fnr, TilgangskontrollAction.update);
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
        return veilarbabacClient.sjekkTilgang(
                innloggetNavAnsatt,
                fnr.asString(),
                action
        );
    }

}