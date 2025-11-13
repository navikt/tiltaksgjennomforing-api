package no.nav.tag.tiltaksgjennomforing.avtale;

import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "tiltaksgjennomforing.mentor-tilskuddsperioder.enabled=true"
})
@ActiveProfiles(Miljø.TEST)
public class MentorAvtaleBeregningStrategyTest {

    @BeforeEach
    public void setup() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;
    }

    @AfterEach
    public void tearDown() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;
    }

    @Test
    public void en_mentor_avtale_beregning_se_at_otpSats_blir_lagret_i_avtale_innhold_etter_beregning() {
        Now.fixedDate(LocalDate.of(2025, 3, 1));
        LocalDate fra = LocalDate.of(2025, 3, 1);
        LocalDate til = LocalDate.of(2025, 3, 31);
        final int antallTimer = 8;
        final int timelonn = 100;
        final int feriepengerBelop = 18;
        final int forventetOptBelop = 164;
        final int arbeidsgiverAvgiftBelop = 12;
        final int forventetBeløpForPeriode = timelonn * antallTimer;
        Avtale avtale = TestData.enMentorAvtaleUsignert();
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        endreAvtale.setStartDato(fra);
        endreAvtale.setSluttDato(til);
        endreAvtale.setOtpSats(0.2);
        endreAvtale.setFeriepengesats(BigDecimal.valueOf(0.023));
        endreAvtale.setArbeidsgiveravgift(BigDecimal.valueOf(0.012));
        endreAvtale.setMentorAntallTimer(8.0);
        endreAvtale.setMentorTimelonn(100);
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER, null);

        assertThat(avtale.getGjeldendeInnhold().getFeriepengerBelop()).isEqualTo(feriepengerBelop);
        assertThat(avtale.getGjeldendeInnhold().getOtpBelop()).isEqualTo(forventetOptBelop);
        assertThat(avtale.getGjeldendeInnhold().getArbeidsgiveravgiftBelop()).isEqualTo(arbeidsgiverAvgiftBelop);
        assertThat(avtale.beregnTilskuddsbeløpForPeriode(fra, til)).isEqualTo(forventetBeløpForPeriode);
        assertThat(avtale.getTilskuddPeriode().size()).isEqualTo(1);
        int forventetSumLonnstilskuddaForMentorAvtaler = forventetBeløpForPeriode + feriepengerBelop + forventetOptBelop + arbeidsgiverAvgiftBelop;
        assertThat(avtale.getGjeldendeInnhold().getSumLonnsutgifter()).isEqualTo(
            forventetSumLonnstilskuddaForMentorAvtaler);
        Now.resetClock();
    }
}
