package no.nav.tag.tiltaksgjennomforing.exceptions;

import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;

public class IkkeTilgangTilDeltakerException extends FeilkodeException {
    private final Fnr identifikator;

    public IkkeTilgangTilDeltakerException(Fnr identifikator) {
        super(Feilkode.IKKE_TILGANG_TIL_DELTAKER);
        this.identifikator = identifikator;
    }

    public IkkeTilgangTilDeltakerException(Fnr identifikator, Feilkode feilkode) {
        super(feilkode);
        this.identifikator = identifikator;
    }

    public Fnr getFnr() {
        return identifikator;
    }
}
