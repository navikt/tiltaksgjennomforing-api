package no.nav.tag.tiltaksgjennomforing.domene;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Value;

@Value
public class NavEnhet implements Identifikator{
    private final String enhetId;

    public NavEnhet(String enhetId) {
        this.enhetId = enhetId;
    }


    @JsonValue
    @Override
    public String asString() {return enhetId;}
}
