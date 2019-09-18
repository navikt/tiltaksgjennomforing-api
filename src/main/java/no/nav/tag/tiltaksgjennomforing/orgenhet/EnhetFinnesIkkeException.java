package no.nav.tag.tiltaksgjennomforing.orgenhet;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EnhetFinnesIkkeException extends RuntimeException {
    public EnhetFinnesIkkeException() {
        super("Finnes ikke i Enhetsregisteret.");
    }
}
