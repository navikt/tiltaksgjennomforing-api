package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon.request.Variables;

@Value
public class NotifikasjonerArbeidsgiverRequest {
    private final String query;
    private final Variables variables;
}