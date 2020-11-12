package no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac;

import no.nav.tag.tiltaksgjennomforing.exceptions.IkkeTilgangTilDeltakerException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetVeileder;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;

@Service
@RequiredArgsConstructor
public class TilgangskontrollService {

    private final VeilarbabacClient veilarbabacClient;

    public boolean harLesetilgangTilKandidat(InnloggetVeileder innloggetVeileder, Fnr fnr) {
        return hentTilgang(innloggetVeileder, fnr, TilgangskontrollAction.read);
    }

    public boolean harSkrivetilgangTilKandidat(InnloggetVeileder innloggetVeileder, Fnr fnr) {
        return hentTilgang(innloggetVeileder, fnr, TilgangskontrollAction.update);
    }

    public void sjekkLesetilgangTilKandidat(InnloggetVeileder innloggetVeileder, Fnr fnr) {
        sjekkTilgang(innloggetVeileder, fnr, TilgangskontrollAction.read);
    }
        
    public void sjekkSkrivetilgangTilKandidat(InnloggetVeileder innloggetVeileder, Fnr fnr) {
        sjekkTilgang(innloggetVeileder, fnr, TilgangskontrollAction.update);
    }

    private void sjekkTilgang(InnloggetVeileder innloggetVeileder, Fnr fnr, TilgangskontrollAction action) {
        if (!hentTilgang(innloggetVeileder, fnr, action)) {
            throw new IkkeTilgangTilDeltakerException();
        }
    }

    private boolean hentTilgang(InnloggetVeileder innloggetVeileder, Fnr fnr, TilgangskontrollAction action) {
        return veilarbabacClient.sjekkTilgang(
                innloggetVeileder,
                fnr.asString(),
                action
        );
    }

}