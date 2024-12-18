package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class EndreStillingsbeskrivelse {
    String stillingstittel;
    String arbeidsoppgaver;
    Integer stillingStyrk08;
    Integer stillingKonseptId;
    Double stillingprosent;
    Double antallDagerPerUke;
}
