package no.nav.tag.tiltaksgjennomforing.enhet;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class VeilarbArenaException extends RuntimeException {

    public VeilarbArenaException() {
        super();
    }

    public VeilarbArenaException(String s) {
        super(s);
    }

    public VeilarbArenaException(String s, HttpClientErrorException e) {
        super(s, e);
    }
}
