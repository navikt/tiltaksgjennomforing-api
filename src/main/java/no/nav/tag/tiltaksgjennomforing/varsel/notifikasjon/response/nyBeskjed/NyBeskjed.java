package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.nyBeskjed;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class NyBeskjed {
    @JsonProperty("__typename")
    String __typename;
    String id;
    String feilmelding;
}
