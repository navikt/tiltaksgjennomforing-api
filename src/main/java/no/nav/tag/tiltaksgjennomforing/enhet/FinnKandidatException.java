package no.nav.tag.tiltaksgjennomforing.enhet;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class FinnKandidatException extends RuntimeException {

    public FinnKandidatException() {
        super();
    }

    public FinnKandidatException(String s) {
        super(s);
    }

    public FinnKandidatException(String s, HttpClientErrorException e) {
        super(s, e);
    }
}
