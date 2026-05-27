package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Builder;
import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;

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
    LonnstilskuddFormaal lonnstilskuddFormaal;
    Stillingstype stillingstype;

    public boolean harMangler(Tiltakstype tiltakstype) {
        boolean harMangler =  Utils.erNoenTomme(
            stillingstittel,
            arbeidsoppgaver,
            stillingStyrk08,
            stillingKonseptId,
            stillingprosent,
            antallDagerPerUke
        );
        return switch (tiltakstype) {
            case VARIG_LONNSTILSKUDD, MIDLERTIDIG_LONNSTILSKUDD, FIREARIG_LONNSTILSKUDD ->
                harMangler || Utils.erNoenTomme(stillingstype, lonnstilskuddFormaal);
            case VTAO ->
                harMangler || Utils.erNoenTomme(stillingstype);
            case ARBEIDSTRENING, INKLUDERINGSTILSKUDD, MENTOR, SOMMERJOBB  ->
                harMangler;
        };
    }
}
