package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.nav.tag.tiltaksgjennomforing.avtale.MentorValgtLonnstype;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class MentorTimelonnBeregning {
    private static final BigDecimal ARBEIDSTIMER_I_AARET = BigDecimal.valueOf(1950);
    private static final BigDecimal MANEDER_I_AARET = BigDecimal.valueOf(12);
    private static final BigDecimal UKER_I_AARET = BigDecimal.valueOf(52);
    private static final BigDecimal ARBEIDSDAGER_I_AARET = BigDecimal.valueOf(260);
    private static final BigDecimal HUNDRE = BigDecimal.valueOf(100);
    private static final int BEREGNING_SKALA = 10;

    public static int beregnMentorTimelonn(MentorValgtLonnstype mentorValgtLonnstype, int mentorValgtLonnstypeBelop, BigDecimal stillingsprosent) {
        if (mentorValgtLonnstypeBelop < 0) {
            throw new IllegalArgumentException("mentorValgtLonnstypeBelop < 0");
        }

        if(mentorValgtLonnstype == MentorValgtLonnstype.TIMELØNN) {
            return mentorValgtLonnstypeBelop;
        }

        if (stillingsprosent == null || stillingsprosent.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }

        if (stillingsprosent.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("stillingsprosent < 0");
        }

        BigDecimal belop = BigDecimal.valueOf(mentorValgtLonnstypeBelop);
        BigDecimal stillingsprosentFaktor = HUNDRE.divide(stillingsprosent, BEREGNING_SKALA, RoundingMode.HALF_UP);

        BigDecimal timelonn = switch (mentorValgtLonnstype) {
            case ÅRSLØNN -> belop
                .divide(ARBEIDSTIMER_I_AARET, BEREGNING_SKALA, RoundingMode.HALF_UP)
                .multiply(stillingsprosentFaktor);
            case MÅNEDSLØNN -> belop
                .divide(ARBEIDSTIMER_I_AARET.divide(MANEDER_I_AARET, BEREGNING_SKALA, RoundingMode.HALF_UP), BEREGNING_SKALA, RoundingMode.HALF_UP)
                .multiply(stillingsprosentFaktor);
            case UKELØNN -> belop
                .divide(ARBEIDSTIMER_I_AARET.divide(UKER_I_AARET, BEREGNING_SKALA, RoundingMode.HALF_UP), BEREGNING_SKALA, RoundingMode.HALF_UP)
                .multiply(stillingsprosentFaktor);
            case DAGSLØNN -> belop
                .divide(ARBEIDSTIMER_I_AARET.divide(ARBEIDSDAGER_I_AARET, BEREGNING_SKALA, RoundingMode.HALF_UP), BEREGNING_SKALA, RoundingMode.HALF_UP)
                .multiply(stillingsprosentFaktor);
            case TIMELØNN -> belop;
        };

        return timelonn.setScale(0, RoundingMode.HALF_UP).intValue();
    }
}
