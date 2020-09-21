package no.nav.tag.tiltaksgjennomforing.exceptions;

public class AvtaleErIkkeAkseptertException extends FeilkodeException {
    public AvtaleErIkkeAkseptertException() {
        super(Feilkode.IKKE_AKSEPTERT_UTKAST);
    }
}
