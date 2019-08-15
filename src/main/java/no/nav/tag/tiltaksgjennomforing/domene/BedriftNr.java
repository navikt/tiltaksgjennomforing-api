package no.nav.tag.tiltaksgjennomforing.domene;

import static no.bekk.bekkopen.org.OrganisasjonsnummerValidator.isValid;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TiltaksgjennomforingException;

@Value
public class BedriftNr implements Identifikator {

    private final String bedriftNr;

    public BedriftNr(String bedriftNr) {
        if (!erGyldigBedriftNr(bedriftNr)) {
            throw new TiltaksgjennomforingException("Ugyldig bedriftsnummer.");
        }
        this.bedriftNr = bedriftNr;
    }

    public static boolean erGyldigBedriftNr(String bedriftNr) {
        return isValid(bedriftNr);
    }

    @Override
    @JsonValue
    public String asString() {
        return bedriftNr;
    }
}
