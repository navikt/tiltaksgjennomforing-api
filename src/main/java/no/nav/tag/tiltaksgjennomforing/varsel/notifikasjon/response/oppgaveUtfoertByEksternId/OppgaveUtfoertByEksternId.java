package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response.oppgaveUtfoertByEksternId;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class OppgaveUtfoertByEksternId {
    @JsonProperty("__typename")
    String __typename;
    String id;
    String feilmelding;
}
