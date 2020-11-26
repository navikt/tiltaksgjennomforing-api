package no.nav.tag.tiltaksgjennomforing.refusjon;

import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;

public class RefusjonFeilException extends RuntimeException {
    public RefusjonFeilException(String melding) {
        super(melding);
    }
}
