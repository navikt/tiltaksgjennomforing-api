package no.nav.tag.tiltaksgjennomforing.domene.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EnhetErIkkeVirksomhetException extends RuntimeException {
    public EnhetErIkkeVirksomhetException() {
        super("Avtale må registreres på virksomhetens bedriftsnummer, ikke den juridisk enheten.");
    }
}
