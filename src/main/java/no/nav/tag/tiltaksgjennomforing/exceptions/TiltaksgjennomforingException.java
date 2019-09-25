package no.nav.tag.tiltaksgjennomforing.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TiltaksgjennomforingException extends RuntimeException {

    public TiltaksgjennomforingException(String message) {
        super(message);
    }

    public TiltaksgjennomforingException(String message, Throwable cause) {
        super(message, cause);
    }
}
