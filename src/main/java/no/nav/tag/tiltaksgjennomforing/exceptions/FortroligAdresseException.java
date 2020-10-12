package no.nav.tag.tiltaksgjennomforing.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.PRECONDITION_FAILED)
public class FortroligAdresseException extends RuntimeException{
}
