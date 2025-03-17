package no.nav.tag.tiltaksgjennomforing.exceptions;

public class Kode6SperretForOpprettelseOgEndringException extends FeilkodeException {
    public Kode6SperretForOpprettelseOgEndringException() {
        super(Feilkode.IKKE_TILGANG_TIL_DELTAKER);
    }
}
