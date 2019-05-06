package no.nav.tag.tiltaksgjennomforing.domene.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EnhetErJuridiskException extends RuntimeException {
    public EnhetErJuridiskException() {
        super("Organisasjonsnummeret kan ikke peke på juridisk enhet.");
    }
}
