package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.persondata.Variables;


@Value
public class NotifikasjonerArbeidsgiverRequest {
    private final String query;
}
