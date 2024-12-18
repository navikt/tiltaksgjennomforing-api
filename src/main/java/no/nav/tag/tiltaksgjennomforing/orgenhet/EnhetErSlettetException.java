package no.nav.tag.tiltaksgjennomforing.orgenhet;

import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;

public class EnhetErSlettetException extends FeilkodeException {
    public EnhetErSlettetException() {
        super(Feilkode.ENHET_ER_SLETTET);
    }
}
