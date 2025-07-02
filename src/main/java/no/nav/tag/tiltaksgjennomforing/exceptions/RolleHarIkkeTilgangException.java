package no.nav.tag.tiltaksgjennomforing.exceptions;

public class RolleHarIkkeTilgangException extends FeilkodeException {
    public RolleHarIkkeTilgangException() {
        super(Feilkode.ROLLE_HAR_IKKE_TILGANG);
    }
}
