package no.nav.tag.tiltaksgjennomforing.domene;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Value;

@Value
public class BedriftNr implements Identifikator {
    private final String bedriftNr;

    @Override
    @JsonValue
    public String asString() {
        return bedriftNr;
    }
}
