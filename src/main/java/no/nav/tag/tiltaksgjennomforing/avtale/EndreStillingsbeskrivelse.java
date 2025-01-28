package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class EndreStillingsbeskrivelse {
    String stillingstittel;
    String arbeidsoppgaver;
    Integer stillingStyrk08;
    Integer stillingKonseptId;
    BigDecimal stillingprosent;
    BigDecimal antallDagerPerUke;
}
