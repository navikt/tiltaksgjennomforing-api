package no.nav.tag.tiltaksgjennomforing.postadresse.exception;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ResponseStatus(BAD_REQUEST)
public class UkjentAdresseException extends RegoppslagFunctionalException {
	public UkjentAdresseException(String message) {
		super(message);
	}
}
