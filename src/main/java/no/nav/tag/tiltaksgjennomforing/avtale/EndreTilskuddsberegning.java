package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class EndreTilskuddsberegning {
    Integer manedslonn;
    BigDecimal feriepengesats;
    BigDecimal arbeidsgiveravgift;
    Double otpSats;
}
