package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class EndreTilskuddsberegning {
    Integer lonnstilskuddProsent;
    Integer manedslonn;
    BigDecimal feriepengesats;
    BigDecimal arbeidsgiveravgift;
    Boolean harFamilietilknytning;
    String familietilknytningForklaring;
    Double otpSats;
    Stillingstype stillingstype;
}
