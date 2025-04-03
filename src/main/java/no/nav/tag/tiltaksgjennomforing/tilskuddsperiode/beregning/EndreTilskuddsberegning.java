package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder(toBuilder = true)
public class EndreTilskuddsberegning {
    Integer manedslonn;
    BigDecimal feriepengesats;
    BigDecimal arbeidsgiveravgift;
    Double otpSats;
    Integer lonnstilskuddProsent;

}
