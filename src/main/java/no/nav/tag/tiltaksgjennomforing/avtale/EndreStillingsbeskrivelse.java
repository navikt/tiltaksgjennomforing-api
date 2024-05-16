package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Value;

@Value
public class EndreStillingsbeskrivelse {
    String stillingstittel;
    String arbeidsoppgaver;
    Integer stillingStyrk08;
    Integer stillingKonseptId;
    Integer stillingprosent;
    Integer antallDagerPerUke;
}
