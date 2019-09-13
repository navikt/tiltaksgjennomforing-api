package no.nav.tag.tiltaksgjennomforing.orgenhet;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EnhetErOrganisasjonsleddException extends RuntimeException {
    public EnhetErOrganisasjonsleddException() {
        super("Avtale må registreres på virksomhetens bedriftsnummer, ikke organisasjonsleddet.");
    }
}
