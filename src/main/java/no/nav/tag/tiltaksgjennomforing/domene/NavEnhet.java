package no.nav.tag.tiltaksgjennomforing.domene;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TiltaksgjennomforingException;

@Value
public class NavEnhet implements Identifikator{
    private final String enhetId;

    public NavEnhet(String enhetId) {
        if (!erGyldigNavEnhet(enhetId)) {
            throw new TiltaksgjennomforingException("Ugyldig format på NAV Enhet");
        }
        this.enhetId = enhetId;
    }

    public static boolean erGyldigNavEnhet(String id) {
        return true;
    }


    @JsonValue
    @Override
    public String asString() {return enhetId;}
}
