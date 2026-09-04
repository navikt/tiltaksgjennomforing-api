package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.softDeleteNotifikasjonByEksternId;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class SoftDeleteNotifikasjonByEksternId {
    @JsonProperty("__typename")
    String __typename;
    String id;
    String feilmelding;
}
