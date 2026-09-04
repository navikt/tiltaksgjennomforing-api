package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class FellesResponse {
    @JsonProperty("__typename")
    String __typename;
    String id;
    String feilmelding;
}
