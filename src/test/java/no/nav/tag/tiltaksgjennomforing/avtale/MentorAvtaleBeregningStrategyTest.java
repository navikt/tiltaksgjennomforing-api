package no.nav.tag.tiltaksgjennomforing.avtale;

import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.EndreTilskuddsberegning;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.MentorBeregningStrategy;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "tiltaksgjennomforing.mentor-tilskuddsperioder.enabled=true"
})
@ActiveProfiles(Miljø.TEST)
public class MentorAvtaleBeregningStrategyTest {
    private final MentorBeregningStrategy mentorBeregningStrategy = new MentorBeregningStrategy();

    @BeforeEach
    public void setup() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;
    }

    @AfterEach
    public void tearDown() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;
        Now.resetClock();
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

    @Test
    public void forleng_mentor_avtale_med_godkjent_tilskuddsperiode_skal_generere_nye_perioder() {
        // Opprett mentor-avtale med alle godkjenninger
        Avtale avtale = TestData.enMentorAvtaleSignertAvAlle();
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.SPESIELT_TILPASSET_INNSATS);
        Now.fixedDate(avtale.getGjeldendeInnhold().getSluttDato().plusDays(1));

        // Verifiser at vi har tilskuddsperioder før forlenging
        int antallPerioderFørForlenging = avtale.getTilskuddPeriode().size();
        assertThat(antallPerioderFørForlenging).isGreaterThan(0);

        // Godkjenn en tilskuddsperiode for å inngå avtalen før forlenging
        Beslutter beslutter = TestData.enBeslutter(avtale);
        beslutter.godkjennTilskuddsperiode(avtale, TestData.ENHET_OPPFØLGING.getVerdi());

        // Forleng avtalen med 2 måneder
        LocalDate nySluttDato = avtale.getGjeldendeInnhold().getSluttDato().plusMonths(2);
        avtale.forlengAvtale(nySluttDato, TestData.enNavIdent());

        // Verifiser at nye tilskuddsperioder ble generert
        Set<TilskuddPeriode> tilskuddPerioderEtterForlenging = avtale.getTilskuddPeriode();
        assertThat(tilskuddPerioderEtterForlenging.size()).isGreaterThan(antallPerioderFørForlenging);

        // Verifiser at nye perioder dekker den forlengede perioden
        TilskuddPeriode sistePeriode = tilskuddPerioderEtterForlenging.stream()
            .max(java.util.Comparator.comparing(TilskuddPeriode::getSluttDato))
            .orElseThrow();
        assertThat(sistePeriode.getSluttDato()).isEqualTo(nySluttDato);
    }

    @Test
    public void reberegnTotal_nullstiller_beregningsfelter_når_otpSats_mangler() {
        Avtale avtale = TestData.enMentorAvtaleUsignert();
        EndreAvtale endreAvtale = TestData.endrePåAlleMentorFelter();
        endreAvtale.setOtpSats(null);
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER, null);

        assertThat(avtale.getGjeldendeInnhold().getManedslonn()).isNull();
        assertThat(avtale.getGjeldendeInnhold().getFeriepengerBelop()).isNull();
        assertThat(avtale.getGjeldendeInnhold().getOtpBelop()).isNull();
        assertThat(avtale.getGjeldendeInnhold().getArbeidsgiveravgiftBelop()).isNull();
        assertThat(avtale.getGjeldendeInnhold().getSumLonnsutgifter()).isNull();
    }

    @Test
    public void reberegnTotal_nullstiller_beregningsfelter_når_feriepengesats_mangler() {
        Avtale avtale = TestData.enMentorAvtaleUsignert();
        EndreAvtale endreAvtale = TestData.endrePåAlleMentorFelter();
        endreAvtale.setFeriepengesats(null);
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER, null);

        assertThat(avtale.getGjeldendeInnhold().getManedslonn()).isNull();
        assertThat(avtale.getGjeldendeInnhold().getFeriepengerBelop()).isNull();
        assertThat(avtale.getGjeldendeInnhold().getOtpBelop()).isNull();
        assertThat(avtale.getGjeldendeInnhold().getArbeidsgiveravgiftBelop()).isNull();
        assertThat(avtale.getGjeldendeInnhold().getSumLonnsutgifter()).isNull();
    }

    @Test
    public void reberegnTotal_nullstiller_beregningsfelter_når_arbeidsgiveravgift_mangler() {
        Avtale avtale = TestData.enMentorAvtaleUsignert();
        EndreAvtale endreAvtale = TestData.endrePåAlleMentorFelter();
        endreAvtale.setArbeidsgiveravgift(null);
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER, null);

        assertThat(avtale.getGjeldendeInnhold().getManedslonn()).isNull();
        assertThat(avtale.getGjeldendeInnhold().getFeriepengerBelop()).isNull();
        assertThat(avtale.getGjeldendeInnhold().getOtpBelop()).isNull();
        assertThat(avtale.getGjeldendeInnhold().getArbeidsgiveravgiftBelop()).isNull();
        assertThat(avtale.getGjeldendeInnhold().getSumLonnsutgifter()).isNull();
    }

    @Test
    public void reberegnTotal_nullstiller_beregningsfelter_når_mentorAntallTimer_mangler() {
        Avtale avtale = TestData.enMentorAvtaleUsignert();
        EndreAvtale endreAvtale = TestData.endrePåAlleMentorFelter();
        endreAvtale.setMentorAntallTimer(null);
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER, null);

        assertThat(avtale.getGjeldendeInnhold().getManedslonn()).isNull();
        assertThat(avtale.getGjeldendeInnhold().getFeriepengerBelop()).isNull();
        assertThat(avtale.getGjeldendeInnhold().getOtpBelop()).isNull();
        assertThat(avtale.getGjeldendeInnhold().getArbeidsgiveravgiftBelop()).isNull();
        assertThat(avtale.getGjeldendeInnhold().getSumLonnsutgifter()).isNull();
    }

    @Test
    public void reberegnTotal_beregner_korrekt_når_alle_nødvendige_felter_er_utfylt() {
        Avtale avtale = TestData.enMentorAvtaleUsignert();
        EndreAvtale endreAvtale = TestData.endrePåAlleMentorFelter();
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER, null);

        assertThat(avtale.getGjeldendeInnhold().getManedslonn()).isNotNull().isPositive();
        assertThat(avtale.getGjeldendeInnhold().getFeriepengerBelop()).isNotNull().isPositive();
        assertThat(avtale.getGjeldendeInnhold().getOtpBelop()).isNotNull().isPositive();
        assertThat(avtale.getGjeldendeInnhold().getArbeidsgiveravgiftBelop()).isNotNull().isPositive();
        assertThat(avtale.getGjeldendeInnhold().getSumLonnsutgifter()).isNotNull().isPositive();
    }

    @Test
    public void nødvendigeFelterErUtfyltForBeregning_returnerer_false_når_ett_felt_mangler() {
        Avtale avtale = TestData.enMentorAvtaleUsignert();
        EndreAvtale endreAvtale = TestData.endrePåAlleMentorFelter();
        endreAvtale.setOtpSats(null);
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER, null);

        assertThat(mentorBeregningStrategy.nødvendigeFelterErUtfyltForBeregningAvTilskuddsbeløp(avtale)).isFalse();
    }

    @Test
    public void nødvendigeFelterErUtfyltForBeregning_returnerer_true_når_alle_felt_er_satt() {
        Avtale avtale = TestData.enMentorAvtaleUsignert();
        EndreAvtale endreAvtale = TestData.endrePåAlleMentorFelter();
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER, null);

        assertThat(mentorBeregningStrategy.nødvendigeFelterErUtfyltForBeregningAvTilskuddsbeløp(avtale)).isTrue();
    }

    @Test
    public void endreBeregning_oppdaterer_alle_beregningsfelter_og_reberegner() {
        Avtale avtale = TestData.enMentorAvtaleSignert();
        int sumFør = avtale.getGjeldendeInnhold().getSumLonnsutgifter();

        EndreTilskuddsberegning endring = EndreTilskuddsberegning.builder()
            .mentorAntallTimer(20.0)
            .otpSats(0.05)
            .arbeidsgiveravgift(BigDecimal.valueOf(0.141))
            .feriepengesats(BigDecimal.valueOf(0.12))
            .mentorValgtLonnstype(MentorValgtLonnstype.ÅRSLØNN)
            .mentorValgtLonnstypeBelop(600_000)
            .stillingprosent(BigDecimal.valueOf(100))
            .build();

        mentorBeregningStrategy.endreBeregning(avtale, endring);

        assertThat(avtale.getGjeldendeInnhold().getOtpSats()).isEqualTo(endring.getOtpSats());
        assertThat(avtale.getGjeldendeInnhold().getFeriepengesats()).isEqualTo(endring.getFeriepengesats());
        assertThat(avtale.getGjeldendeInnhold().getArbeidsgiveravgift()).isEqualTo(endring.getArbeidsgiveravgift());
        assertThat(avtale.getGjeldendeInnhold().getMentorAntallTimer()).isEqualTo(endring.getMentorAntallTimer());
        assertThat(avtale.getGjeldendeInnhold().getSumLonnsutgifter()).isNotEqualTo(sumFør);
        assertThat(avtale.getGjeldendeInnhold().getSumLonnsutgifter()).isNotNull().isPositive();
    }
}
