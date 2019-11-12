package no.nav.tag.tiltaksgjennomforing.exceptions;

public class StartDatoErEtterSluttDatoException extends TiltaksgjennomforingException {
    public StartDatoErEtterSluttDatoException() {
        super("Startdato er etter sluttdato");
    }
}
