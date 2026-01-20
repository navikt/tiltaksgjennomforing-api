package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder(toBuilder = true)
public class EndreTilskuddsberegning {
    //Lønnstilskudd / VTAO
    Integer lonnstilskuddProsent;
    Integer manedslonn;
    BigDecimal feriepengesats;
    BigDecimal arbeidsgiveravgift;
    Double otpSats;
    //Mentor
    Integer mentorTimelonn;
    Double mentorAntallTimer;
    Double mentorAntallTimerPerMnd;
}
