package no.nav.tag.tiltaksgjennomforing.exceptions;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;

public class IkkeTilgangTilAvtaleException extends FeilkodeException {
    private final Avtale avtale;

    public IkkeTilgangTilAvtaleException(Avtale avtale) {
        super(Feilkode.IKKE_TILGANG_TIL_AVTALE);
        this.avtale = avtale;
    }

    public Avtale getAvtale() {
        return avtale;
    }
}
