package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.response;

import lombok.Value;

@Value
public class FellesMutationResponse {
    String __typename;
    String id;
    String feilmelding;
}
