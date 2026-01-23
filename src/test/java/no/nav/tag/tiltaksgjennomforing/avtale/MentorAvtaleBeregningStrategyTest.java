package no.nav.tag.tiltaksgjennomforing.avtale;

import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.EndreTilskuddsberegning;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.IntStream;

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
        final int feriepengerBelop = 722;
        final int forventetOptBelop = 780;
        final int arbeidsgiverAvgiftBelop = 438;
        final int forventetBeløpForPeriode = 9020;
        Avtale avtale = TestData.enMentorAvtaleUsignert();
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        endreAvtale.setStartDato(fra);
        endreAvtale.setSluttDato(til);
        endreAvtale.setStillingprosent(BigDecimal.valueOf(50.7));
        endreAvtale.setMentorValgtLonnstype(MentorValgtLonnstype.ÅRSLØNN);
        endreAvtale.setMentorValgtLonnstypeBelop(875000);
        endreAvtale.setOtpSats(0.1);
        endreAvtale.setFeriepengesats(BigDecimal.valueOf(0.102));
        endreAvtale.setArbeidsgiveravgift(BigDecimal.valueOf(0.051));
        endreAvtale.setMentorAntallTimer(8.0);
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER, null);

        assertThat(avtale.getGjeldendeInnhold().getFeriepengerBelop()).isEqualTo(feriepengerBelop);
        assertThat(avtale.getGjeldendeInnhold().getOtpBelop()).isEqualTo(forventetOptBelop);
        assertThat(avtale.getGjeldendeInnhold().getArbeidsgiveravgiftBelop()).isEqualTo(arbeidsgiverAvgiftBelop);
        assertThat(avtale.beregnTilskuddsbeløpForPeriode(fra, til)).isEqualTo(forventetBeløpForPeriode);
        assertThat(avtale.getTilskuddPeriode().size()).isEqualTo(1);
        assertThat(avtale.getGjeldendeInnhold().getSumLonnsutgifter()).isEqualTo(forventetBeløpForPeriode);
        Now.resetClock();
    }

    @Test
    public void endre_endreTilskuddsberegning_for_mentor_med_flere_tilskuddsperioder() {
        Now.fixedDate(LocalDate.of(2026, 1, 1));
        Avtale avtale = TestData.enMentorAvtaleSignert();
        avtale.godkjennForDeltaker(avtale.getDeltakerFnr());
        avtale.godkjennForArbeidsgiver(avtale.getFnrOgBedrift().deltakerFnr());
        avtale.godkjennForVeileder(avtale.getVeilederNavIdent());

        assertThat(avtale.getGjeldendeInnhold().getVersjon()).isEqualTo(1);
        assertThat(avtale.getTilskuddPeriode()
            .stream()
            .flatMapToInt(tilskuddPeriode -> IntStream.of(tilskuddPeriode.getBeløp()))
            .sum()).isEqualTo(57072);

        EndreTilskuddsberegning endreTilskuddsberegning = EndreTilskuddsberegning.builder()
            .mentorAntallTimer(14.0)
            .otpSats(0.13)
            .arbeidsgiveravgift(BigDecimal.valueOf(0.106))
            .feriepengesats(BigDecimal.valueOf(0.143))
            .mentorValgtLonnstype(MentorValgtLonnstype.ÅRSLØNN)
            .mentorValgtLonnstypeBelop(780000)
            .stillingprosent(BigDecimal.valueOf(100))
            .build();

        avtale.endreTilskuddsberegning(endreTilskuddsberegning, TestData.enNavIdent());

        assertThat(avtale.getGjeldendeInnhold().getVersjon()).isEqualTo(2);
        assertThat(avtale.erGodkjentAvVeileder()).isTrue();
        assertThat(avtale.getGjeldendeInnhold().getOtpSats()).isEqualTo(endreTilskuddsberegning.getOtpSats());
        assertThat(avtale.getGjeldendeInnhold()
            .getFeriepengesats()).isEqualTo(endreTilskuddsberegning.getFeriepengesats());
        assertThat(avtale.getGjeldendeInnhold()
            .getArbeidsgiveravgift()).isEqualTo(endreTilskuddsberegning.getArbeidsgiveravgift());
        assertThat(avtale.getGjeldendeInnhold()
            .getArbeidsgiveravgift()).isEqualTo(endreTilskuddsberegning.getArbeidsgiveravgift());
        assertThat(avtale.getTilskuddPeriode().stream().mapToInt(TilskuddPeriode::getBeløp).sum()).isEqualTo(48000);
    }
}
