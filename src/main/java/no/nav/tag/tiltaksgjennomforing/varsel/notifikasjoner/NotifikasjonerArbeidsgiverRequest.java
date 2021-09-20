package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjoner;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.persondata.Variables;


@Value
public class NotifikasjonerArbeidsgiverRequest {
    private final String query;
    private final Variables variables;
}
