package no.nav.tag.tiltaksgjennomforing.exceptions;

import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;

public class IkkeTilgangTilDeltakerException extends FeilkodeException {
    private final Fnr identifikator;

    public IkkeTilgangTilDeltakerException(Fnr identifikator, Feilkode feilkode) {
        super(feilkode);
        this.identifikator = identifikator;
    }

    public IkkeTilgangTilDeltakerException(Fnr identifikator) {
        this(identifikator, Feilkode.IKKE_TILGANG_TIL_DELTAKER);
    }

    public Fnr getFnr() {
        return identifikator;
    }
}
