package no.nav.tag.tiltaksgjennomforing.postadresse.exception;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ResponseStatus(BAD_REQUEST)
public class RegoppslagFunctionalException extends RuntimeException {
	public RegoppslagFunctionalException(String message) {
		super(message);
	}
}
