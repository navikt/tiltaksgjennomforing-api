package no.nav.tag.tiltaksgjennomforing.exceptions;

public abstract class FeilkodeException extends RuntimeException {
    private final Feilkode feilkode;

    protected FeilkodeException(Feilkode feilkode) {
        this.feilkode = feilkode;
    }

    public Feilkode getFeilkode() {
        return feilkode;
    }
}
