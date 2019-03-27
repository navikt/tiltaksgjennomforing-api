package no.nav.tag.tiltaksgjennomforing.domene.exceptions;

public class AltinnException extends RuntimeException {

    public AltinnException(String message) {
        super(message);
    }

    public AltinnException(String message, Exception e) {
        super(message, e);
    }
}
