package no.nav.tag.tiltaksgjennomforing.postadresse.exception;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ResponseStatus(NOT_FOUND)
public class UkjentAdresseException extends RegoppslagFunctionalException {
	public UkjentAdresseException(String message) {
		super(message);
	}
}
