package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import lombok.Builder;
import lombok.Value;
import no.nav.tag.tiltaksgjennomforing.avtale.MentorValgtLonnstype;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.utils.Utils;

import java.math.BigDecimal;
import java.util.Optional;

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
    Integer mentorValgtLonnstypeBelop;
    MentorValgtLonnstype mentorValgtLonnstype;
    BigDecimal stillingprosent;

    public boolean harMangler(Tiltakstype tiltakstype) {
        return switch (tiltakstype) {
            case VARIG_LONNSTILSKUDD -> Utils.erNoenTomme(
                arbeidsgiveravgift,
                feriepengesats,
                otpSats,
                lonnstilskuddProsent,
                manedslonn
            );
            case MENTOR -> Utils.erNoenTomme(
                arbeidsgiveravgift,
                feriepengesats,
                otpSats,
                mentorAntallTimer,
                mentorValgtLonnstype,
                mentorValgtLonnstypeBelop,
                // Timelønn har ikke stillingsprosent, settes derfor default til 0 (slik at den ikke valideres)
                Optional.ofNullable(mentorValgtLonnstype)
                    .map(lonnstype -> lonnstype.erTimelonn() ? BigDecimal.ZERO : stillingprosent)
                    .orElse(null)
            );
            case MIDLERTIDIG_LONNSTILSKUDD, FIREARIG_LONNSTILSKUDD, SOMMERJOBB -> Utils.erNoenTomme(
                arbeidsgiveravgift,
                feriepengesats,
                otpSats,
                manedslonn
            );
            case ARBEIDSTRENING, INKLUDERINGSTILSKUDD, VTAO -> false;
        };
    }
}
