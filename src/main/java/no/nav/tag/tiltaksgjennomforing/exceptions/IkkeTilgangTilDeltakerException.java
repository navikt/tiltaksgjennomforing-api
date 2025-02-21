package no.nav.tag.tiltaksgjennomforing.exceptions;

import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Identifikator;

public class IkkeTilgangTilDeltakerException extends FeilkodeException {
    private final Identifikator identifikator;

    public IkkeTilgangTilDeltakerException(Identifikator identifikator) {
        super(Feilkode.IKKE_TILGANG_TIL_DELTAKER);
        this.identifikator = identifikator;
    }

    public Fnr getFnr() {
        return identifikator instanceof Fnr fnr ? fnr : null;
    }
}
