package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response;

import lombok.Value;

@Value
public class CommonResponse {
    String __typename;
    String id;
    String feilmelding;
}
