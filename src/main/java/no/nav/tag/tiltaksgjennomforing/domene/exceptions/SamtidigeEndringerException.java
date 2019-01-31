package no.nav.tag.tiltaksgjennomforing.domene.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class SamtidigeEndringerException extends RuntimeException {
    public SamtidigeEndringerException(String message) {
        super(message);
    }
}
