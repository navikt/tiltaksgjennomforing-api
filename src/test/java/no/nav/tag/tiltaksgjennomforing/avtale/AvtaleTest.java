package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.autorisasjon.SlettemerkeProperties;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Tilgang;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.avtale.RefusjonKontaktperson.Fields;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging.VeilarboppfolgingService;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilLonnstilskuddsprosentException;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.SamtidigeEndringerException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import no.nav.tag.tiltaksgjennomforing.exceptions.VarighetForLangArbeidstreningException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;
import no.nav.tag.tiltaksgjennomforing.orgenhet.EregService;
import no.nav.tag.tiltaksgjennomforing.orgenhet.Organisasjon;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.EndreTilskuddsberegning;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import no.nav.team_tiltak.felles.persondata.pdl.domene.Diskresjonskode;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;
import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.enSommerjobbAvtale;
import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.endringPåAlleLønnstilskuddFelterForSommerjobb;
import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.setOppfølgingPåAvtale;
import static no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriodeStatus.GODKJENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AvtaleTest {

    @BeforeEach
    public void setup() {
        Now.resetClock();
    }

    @Test
    public void test_riktig_beregning_Varig_Lonnstilskudd_Avtale_som_varer_i_mange_aar_med_redusert_lønnsilskudd() {
        Now.fixedDate(LocalDate.of(2024, 7, 29));
        Avtale avtale = enSommerjobbAvtale();
        setOppfølgingPåAvtale(avtale);
        EndreAvtale endreAvtale = endringPåAlleLønnstilskuddFelterForSommerjobb(75);
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);

        avtale.setTiltakstype(Tiltakstype.VARIG_LONNSTILSKUDD);
        avtale.getGjeldendeInnhold().setDeltakerFornavn("Lilly");
        avtale.getGjeldendeInnhold().setDeltakerEtternavn("Lønning");
        avtale.getGjeldendeInnhold().setArbeidsgiverKontonummer("22222222222");
        avtale.getGjeldendeInnhold().setManedslonn(20000);
        avtale.getGjeldendeInnhold().setFeriepengesats(BigDecimal.valueOf(0.12));
        avtale.getGjeldendeInnhold().setArbeidsgiveravgift(BigDecimal.valueOf(0.141));
        avtale.getGjeldendeInnhold().setVersjon(1);
        avtale.getGjeldendeInnhold().setJournalpostId("1");
        avtale.getGjeldendeInnhold().setMaal(List.of());
        assertThat(avtale.getGjeldendeInnhold().getSumLonnstilskudd()).isEqualTo(30600);
        endreAvtale = new EndreAvtale();
        endreAvtale.setOppfolging("Telefon hver uke");
        endreAvtale.setTilrettelegging("Ingen");
        endreAvtale.setStartDato(Now.localDate());
        endreAvtale.setSluttDato(endreAvtale.getStartDato().plusYears(6).minusDays(1));
        endreAvtale.setStillingprosent(BigDecimal.valueOf(50.259));
        endreAvtale.setArbeidsoppgaver("Butikkarbeid");
        endreAvtale.setArbeidsgiverKontonummer("000111222");
        endreAvtale.setStillingstittel("Butikkbetjent");
        endreAvtale.setStillingStyrk08(5223);
        endreAvtale.setStillingKonseptId(112968);
        endreAvtale.setLonnstilskuddProsent(75);
        endreAvtale.setManedslonn(10000);
        endreAvtale.setFeriepengesats(BigDecimal.ONE);
        endreAvtale.setArbeidsgiveravgift(BigDecimal.ONE);
        endreAvtale.setOtpSats(0.02);
        endreAvtale.setStillingstype(Stillingstype.FAST);
        endreAvtale.setAntallDagerPerUke(BigDecimal.valueOf(5.0));
        endreAvtale.setRefusjonKontaktperson(new RefusjonKontaktperson("Ola", "Olsen", "12345678", true));
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        final int FORVENTET_ANTALL_TILSKUDDSPERIODER_FOR_6_AAR_VARIG_AVTALE = 74;
        assertThat(avtale.getTilskuddPeriode().stream().map(TilskuddPeriode::getBeløp).toList().size()).isEqualTo(FORVENTET_ANTALL_TILSKUDDSPERIODER_FOR_6_AAR_VARIG_AVTALE);
        assertThat(avtale.getGjeldendeInnhold().getSumLønnstilskuddRedusert()).isEqualTo(27336);
        assertThat(avtale.getGjeldendeInnhold().getDatoForRedusertProsent()).isEqualTo(LocalDate.of(2025,07,29));
    }


    @Test
    public void test_riktig_beregning_SOMMERJOBB_50_prosent_Lonnstilskudd_Avtale_hvor_periode_er_mellom_to_maneder() {
        Now.fixedDate(LocalDate.of(2024, 6, 21));
        Avtale avtale = enSommerjobbAvtale();
        setOppfølgingPåAvtale(avtale);
        EndreAvtale endreAvtale = endringPåAlleLønnstilskuddFelterForSommerjobb(50);
        endreAvtale.setStartDato(LocalDate.of(2024, 6, 23));
        endreAvtale.setSluttDato(LocalDate.of(2024, 7, 20));
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        avtale.getGjeldendeInnhold().setDeltakerFornavn("Lilly");
        avtale.getGjeldendeInnhold().setDeltakerEtternavn("Lønning");
        avtale.getGjeldendeInnhold().setArbeidsgiverKontonummer("22222222222");
        avtale.getGjeldendeInnhold().setManedslonn(20000);
        avtale.getGjeldendeInnhold().setFeriepengesats(BigDecimal.valueOf(0.12));
        avtale.getGjeldendeInnhold().setArbeidsgiveravgift(BigDecimal.valueOf(0.141));
        avtale.getGjeldendeInnhold().setVersjon(1);
        avtale.getGjeldendeInnhold().setJournalpostId(null);
        avtale.getGjeldendeInnhold().setMaal(List.of());

        assertThat(avtale.getGjeldendeInnhold().getVersjon()).isEqualTo(1);
        assertThat(avtale.getTiltakstype()).isEqualTo(Tiltakstype.SOMMERJOBB);
        assertThat(avtale.getTilskuddPeriode().stream().map(TilskuddPeriode::getBeløp).toList()).isEqualTo(List.of(5362, 13405));
    }

    @Test
    public void test_riktig_beregning_Varig_Lonnstilskudd_Avtale_som_varer_i_mange_aar() {
        Now.fixedDate(LocalDate.of(2024, 7, 29));
        Avtale avtale = TestData.enVarigLonnstilskuddAvtaleMedAltUtfylt();
        assertThat(avtale.getGjeldendeInnhold().getSumLonnstilskudd()).isEqualTo(24480);
        assertThat(avtale.getTilskuddPeriode().stream().map(TilskuddPeriode::getBeløp).toList()).isEqualTo(List.of(2413,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                22520));
        EndreAvtale endreAvtale = new EndreAvtale();
        endreAvtale.setOppfolging("Telefon hver uke");
        endreAvtale.setTilrettelegging("Ingen");
        endreAvtale.setStartDato(Now.localDate());
        endreAvtale.setSluttDato(endreAvtale.getStartDato().plusYears(6).minusDays(1));
        endreAvtale.setStillingprosent(BigDecimal.valueOf(50.222));
        endreAvtale.setArbeidsoppgaver("Butikkarbeid");
        endreAvtale.setArbeidsgiverKontonummer("000111222");
        endreAvtale.setStillingstittel("Butikkbetjent");
        endreAvtale.setStillingStyrk08(5223);
        endreAvtale.setStillingKonseptId(112968);
        endreAvtale.setLonnstilskuddProsent(60);
        endreAvtale.setManedslonn(10000);
        endreAvtale.setFeriepengesats(BigDecimal.ONE);
        endreAvtale.setArbeidsgiveravgift(BigDecimal.ONE);
        endreAvtale.setOtpSats(0.02);
        endreAvtale.setStillingstype(Stillingstype.FAST);
        endreAvtale.setAntallDagerPerUke(BigDecimal.valueOf(5.0));
        endreAvtale.setRefusjonKontaktperson(new RefusjonKontaktperson("Ola", "Olsen", "12345678", true));
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        final int FORVENTET_ANTALL_TILSKUDDSPERIODER_FOR_6_AAR_VARIG_AVTALE = 73;
        assertThat(avtale.getTilskuddPeriode().stream().map(TilskuddPeriode::getBeløp).toList().size()).isEqualTo(FORVENTET_ANTALL_TILSKUDDSPERIODER_FOR_6_AAR_VARIG_AVTALE);
        assertThat(avtale.getGjeldendeInnhold().getSumLonnstilskudd()).isEqualTo(24480);
        assertThat(avtale.getTilskuddPeriode().stream().map(TilskuddPeriode::getBeløp).toList()).isEqualTo(List.of(2413,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                22520));
    }

    @Test
    public void test_riktig_beregning_75_prosent_Varig_Lonnstilskudd_Avtale_som_varer_i_ett_aar() {
        Now.fixedDate(LocalDate.of(2024, 7, 29));
        Avtale avtale = TestData.enVarigLonnstilskuddAvtaleMedAltUtfylt();
        assertThat(avtale.getTilskuddPeriode().stream().map(TilskuddPeriode::getBeløp).toList()).isEqualTo(List.of(2413,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                22520));

        EndreAvtale endreAvtale = new EndreAvtale();
        endreAvtale.setOppfolging("Telefon hver uke");
        endreAvtale.setTilrettelegging("Ingen");
        endreAvtale.setStartDato(Now.localDate());
        endreAvtale.setSluttDato(endreAvtale.getStartDato().plusYears(1).minusDays(1));
        endreAvtale.setStillingprosent(BigDecimal.valueOf(100.0));
        endreAvtale.setArbeidsoppgaver("Butikkarbeid");
        endreAvtale.setArbeidsgiverKontonummer("000111222");
        endreAvtale.setStillingstittel("Butikkbetjent");
        endreAvtale.setStillingStyrk08(5223);
        endreAvtale.setStillingKonseptId(112968);
        endreAvtale.setLonnstilskuddProsent(75);
        endreAvtale.setManedslonn(10000);
        endreAvtale.setFeriepengesats(BigDecimal.ONE);
        endreAvtale.setArbeidsgiveravgift(BigDecimal.ONE);
        endreAvtale.setOtpSats(0.02);
        endreAvtale.setStillingstype(Stillingstype.FAST);
        endreAvtale.setAntallDagerPerUke(BigDecimal.valueOf(5.0));
        endreAvtale.setRefusjonKontaktperson(new RefusjonKontaktperson("Ola", "Olsen", "12345678", true));
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        final int FORVENTET_ANTALL_TILSKUDDSPERIODER_FOR_1_AAR_VARIG_AVTALE = 13;
        assertThat(avtale.getTilskuddPeriode().stream().map(TilskuddPeriode::getBeløp).toList().size()).isEqualTo(FORVENTET_ANTALL_TILSKUDDSPERIODER_FOR_1_AAR_VARIG_AVTALE);
        assertThat(avtale.getTilskuddPeriode().stream().map(TilskuddPeriode::getBeløp).toList()).isEqualTo(List.of(3016,
                30600,
                30600,
                30600,
                30600,
                30600,
                30600,
                30600,
                30600,
                30600,
                30600,
                30600,
                28149));
    }

    @Test
    public void test_riktig_beregning_VTAO_Lonnstilskudd_Avtale() {
        Now.fixedDate(LocalDate.of(2024, 7, 29));
        Avtale avtale = TestData.enVtaoAvtaleGodkjentAvArbeidsgiver();
        List<Integer> forventedeBelop = Arrays.asList(671, 6808, 6808, 6808, 6808, 6808, 7067, 7067, 7067, 7067, 7067, 7067, 6501);
        assertThat(avtale.getTilskuddPeriode().stream().map(TilskuddPeriode::getBeløp).toList()).isEqualTo(forventedeBelop);
        avtale.opphevGodkjenningerSomVeileder();
        EndreAvtale endreAvtale = new EndreAvtale();
        endreAvtale.setOppfolging("Telefon hver uke");
        endreAvtale.setTilrettelegging("Ingen");
        endreAvtale.setStartDato(Now.localDate());
        endreAvtale.setSluttDato(endreAvtale.getStartDato().plusYears(1).minusDays(1));
        endreAvtale.setStillingprosent(BigDecimal.valueOf(100.0));
        endreAvtale.setArbeidsoppgaver("Butikkarbeid");
        endreAvtale.setArbeidsgiverKontonummer("000111222");
        endreAvtale.setStillingstittel("Butikkbetjent");
        endreAvtale.setStillingStyrk08(5223);
        endreAvtale.setStillingKonseptId(112968);
        endreAvtale.setLonnstilskuddProsent(75);
        endreAvtale.setManedslonn(10000);
        endreAvtale.setFeriepengesats(BigDecimal.ONE);
        endreAvtale.setArbeidsgiveravgift(BigDecimal.ONE);
        endreAvtale.setOtpSats(0.02);
        endreAvtale.setStillingstype(Stillingstype.FAST);
        endreAvtale.setAntallDagerPerUke(BigDecimal.valueOf(5.0));
        endreAvtale.setRefusjonKontaktperson(new RefusjonKontaktperson("Ola", "Olsen", "12345678", true));
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);

        final int FORVENTET_ANTALL_TILSKUDDSPERIODER_FOR_1_AAR_VARIG_AVTALE = 13;
        assertThat(avtale.getTilskuddPeriode().stream().map(TilskuddPeriode::getBeløp).toList().size()).isEqualTo(FORVENTET_ANTALL_TILSKUDDSPERIODER_FOR_1_AAR_VARIG_AVTALE);
        List<Integer> forventedeBelopEtterEndring = Arrays.asList(671, 6808, 6808, 6808, 6808, 6808, 7067, 7067, 7067, 7067, 7067, 7067, 6501);
        assertThat(avtale.getTilskuddPeriode().stream().map(TilskuddPeriode::getBeløp).toList()).isEqualTo(forventedeBelopEtterEndring);

        double otpSats = 0.048;
        BigDecimal feriepengesats = new BigDecimal("0.166");
        BigDecimal arbeidsgiveravgift = BigDecimal.ZERO;
        int manedslonn = 44444;

        assertThatThrownBy(() -> avtale.endreTilskuddsberegning(EndreTilskuddsberegning.builder().otpSats(otpSats).feriepengesats(feriepengesats).arbeidsgiveravgift(arbeidsgiveravgift).manedslonn(manedslonn).build(), TestData.enNavIdent()))
                .isInstanceOf(FeilkodeException.class);

    }

    @Test
    public void test_riktig_beregning_Midlertidig_Lonnstilskudd_Avtale() {
        Now.fixedDate(LocalDate.of(2024, 7, 29));
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleGodkjentAvVeileder();
        double otpSats = 0.048;
        BigDecimal feriepengesats = new BigDecimal("0.166");
        BigDecimal arbeidsgiveravgift = BigDecimal.ZERO;
        int manedslonn = 44444;

        assertThat(avtale.getGjeldendeInnhold().getVersjon()).isEqualTo(1);
        assertThat(avtale.getTilskuddPeriode().stream().map(TilskuddPeriode::getBeløp).toList()).isEqualTo(List.of(1609,
                16320,
                16320,
                16320,
                16320,
                16320,
                15013,
                1206,
                12240,
                12240,
                12240,
                12240,
                12240,
                11260));
        avtale.endreTilskuddsberegning(EndreTilskuddsberegning.builder().otpSats(otpSats).feriepengesats(feriepengesats).arbeidsgiveravgift(arbeidsgiveravgift).manedslonn(manedslonn).build(), TestData.enNavIdent());

        assertThat(avtale.getGjeldendeInnhold().getVersjon()).isEqualTo(2);
        assertThat(avtale.erGodkjentAvVeileder()).isTrue();
        assertThat(avtale.getGjeldendeInnhold().getSumLonnstilskudd()).isPositive();
        assertThat(avtale.getGjeldendeInnhold().getOtpSats()).isEqualTo(otpSats);
        assertThat(avtale.getGjeldendeInnhold().getFeriepengesats()).isEqualTo(feriepengesats);
        assertThat(avtale.getGjeldendeInnhold().getArbeidsgiveravgift()).isEqualTo(arbeidsgiveravgift);
        assertThat(avtale.getGjeldendeInnhold().getManedslonn()).isEqualTo(manedslonn);
        assertThat(avtale.getTilskuddPeriode().stream().map(TilskuddPeriode::getBeløp).toList()).isEqualTo(List.of(2141,
                21724,
                21724,
                21724,
                21724,
                21724,
                19984,
                1606,
                16293,
                16293,
                16293,
                16293,
                16293,
                14988));
    }

    @Test
    public void test_riktig_beregning_NEDSATT_ARBEIDSEVNE_Midlertidig_Lonnstilskudd_Avtale_som_varer_i_2_aar() {
        Now.fixedDate(LocalDate.of(2024, 7, 29));
        Avtale avtale = TestData.enMidLonnstilskuddAvtaleMedVarigTilpassetSatsMedAltUtfylt();
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setAvtaleInngått(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvNavIdent(TestData.enNavIdent());
        avtale.getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.localDateTime());
        avtale.getGjeldendeInnhold().setJournalpostId("1");
        double otpSats = 0.048;
        BigDecimal feriepengesats = new BigDecimal("0.166");
        BigDecimal arbeidsgiveravgift = BigDecimal.ZERO;
        int manedslonn = 44444;

        assertThat(avtale.getGjeldendeInnhold().getVersjon()).isEqualTo(1);
        assertThat(avtale.getTilskuddPeriode().stream().map(TilskuddPeriode::getBeløp).toList()).isEqualTo(List.of(2413,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                24480,
                22520,
                2011,
                20400,
                20400,
                20400,
                20400,
                20400,
                20400,
                20400,
                20400,
                20400,
                20400,
                18096));
        avtale.endreTilskuddsberegning(EndreTilskuddsberegning.builder().otpSats(otpSats).feriepengesats(feriepengesats).arbeidsgiveravgift(arbeidsgiveravgift).manedslonn(manedslonn).build(), TestData.enNavIdent());

        assertThat(avtale.getGjeldendeInnhold().getVersjon()).isEqualTo(2);
        assertThat(avtale.erGodkjentAvVeileder()).isTrue();
        assertThat(avtale.getGjeldendeInnhold().getSumLonnstilskudd()).isPositive();
        assertThat(avtale.getGjeldendeInnhold().getOtpSats()).isEqualTo(otpSats);
        assertThat(avtale.getGjeldendeInnhold().getFeriepengesats()).isEqualTo(feriepengesats);
        assertThat(avtale.getGjeldendeInnhold().getArbeidsgiveravgift()).isEqualTo(arbeidsgiveravgift);
        assertThat(avtale.getGjeldendeInnhold().getManedslonn()).isEqualTo(manedslonn);
        assertThat(avtale.getTilskuddPeriode().stream().map(TilskuddPeriode::getBeløp).toList()).isEqualTo(List.of(3212,
                32585,
                32585,
                32585,
                32585,
                32585,
                32585,
                32585,
                32585,
                32585,
                32585,
                32585,
                29976,
                2676,
                27155,
                27155,
                27155,
                27155,
                27155,
                27155,
                27155,
                27155,
                27155,
                27155,
                24088));
    }

    @Test
    public void test_riktig_beregning_SOMMERJOBB_50_prosent_Lonnstilskudd_Avtale() {
        Now.fixedDate(LocalDate.of(2024, 6, 1));
        Avtale avtale = TestData.enSommerjobbLonnstilskuddAvtaleGodkjentAvVeileder(50);

        double otpSats = 0.048;
        BigDecimal feriepengesats = new BigDecimal("0.166");
        BigDecimal arbeidsgiveravgift = BigDecimal.ZERO;
        int manedslonn = 44444;

        assertThat(avtale.getGjeldendeInnhold().getVersjon()).isEqualTo(1);
        assertThat(avtale.getTilskuddPeriode().stream().map(TilskuddPeriode::getBeløp).toList()).isEqualTo(List.of(18766));
        avtale.endreTilskuddsberegning(
            EndreTilskuddsberegning.builder()
                .lonnstilskuddProsent(50)
                .otpSats(otpSats)
                .feriepengesats(feriepengesats)
                .arbeidsgiveravgift(arbeidsgiveravgift)
                .manedslonn(manedslonn)
                .build(), TestData.enNavIdent()
        );

        assertThat(avtale.getGjeldendeInnhold().getVersjon()).isEqualTo(2);
        assertThat(avtale.erGodkjentAvVeileder()).isTrue();
        assertThat(avtale.getGjeldendeInnhold().getSumLonnstilskudd()).isPositive();
        assertThat(avtale.getGjeldendeInnhold().getOtpSats()).isEqualTo(otpSats);
        assertThat(avtale.getGjeldendeInnhold().getFeriepengesats()).isEqualTo(feriepengesats);
        assertThat(avtale.getGjeldendeInnhold().getArbeidsgiveravgift()).isEqualTo(arbeidsgiveravgift);
        assertThat(avtale.getGjeldendeInnhold().getManedslonn()).isEqualTo(manedslonn);
        assertThat(avtale.getTilskuddPeriode().stream().map(TilskuddPeriode::getBeløp).toList()).isEqualTo(List.of(24980));
    }

    @Test
    public void test_riktig_beregning_SOMMERJOBB_75_prosent_Lonnstilskudd_Avtale() {
        Now.fixedDate(LocalDate.of(2024, 6, 1));
        Avtale avtale = TestData.enSommerjobbLonnstilskuddAvtaleGodkjentAvVeileder(75);

        double otpSats = 0.048;
        BigDecimal feriepengesats = new BigDecimal("0.166");
        BigDecimal arbeidsgiveravgift = BigDecimal.ZERO;
        int manedslonn = 44444;

        assertThat(avtale.getGjeldendeInnhold().getVersjon()).isEqualTo(1);
        assertThat(avtale.getTilskuddPeriode().stream().map(TilskuddPeriode::getBeløp).toList()).isEqualTo(List.of(28149));
        avtale.endreTilskuddsberegning(
            EndreTilskuddsberegning.builder()
                .lonnstilskuddProsent(75)
                .otpSats(otpSats)
                .feriepengesats(feriepengesats)
                .arbeidsgiveravgift(arbeidsgiveravgift)
                .manedslonn(manedslonn)
                .build(), TestData.enNavIdent()
        );

        assertThat(avtale.getGjeldendeInnhold().getVersjon()).isEqualTo(2);
        assertThat(avtale.erGodkjentAvVeileder()).isTrue();
        assertThat(avtale.getGjeldendeInnhold().getSumLonnstilskudd()).isPositive();
        assertThat(avtale.getGjeldendeInnhold().getOtpSats()).isEqualTo(otpSats);
        assertThat(avtale.getGjeldendeInnhold().getFeriepengesats()).isEqualTo(feriepengesats);
        assertThat(avtale.getGjeldendeInnhold().getArbeidsgiveravgift()).isEqualTo(arbeidsgiveravgift);
        assertThat(avtale.getGjeldendeInnhold().getManedslonn()).isEqualTo(manedslonn);
        assertThat(avtale.getTilskuddPeriode().stream().map(TilskuddPeriode::getBeløp).toList()).isEqualTo(List.of(37470));
    }

    @Test
    public void nyAvtaleFactorySkalReturnereRiktigeStandardverdier() {
        Fnr deltakerFnr = new Fnr("23078637692");

        NavIdent veilederNavIdent = new NavIdent("X123456");
        BedriftNr bedriftNr = new BedriftNr("000111222");
        Avtale avtale = Avtale.opprett(new OpprettAvtale(deltakerFnr, bedriftNr, Tiltakstype.ARBEIDSTRENING), Avtaleopphav.VEILEDER, veilederNavIdent);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(avtale.getOpprettetTidspunkt()).isNotNull();
            softly.assertThat(avtale.getDeltakerFnr()).isEqualTo(deltakerFnr);
            softly.assertThat(avtale.getGjeldendeInnhold().getDeltakerTlf()).isNull();
            softly.assertThat(avtale.getGjeldendeInnhold().getMaal()).isEmpty();
            softly.assertThat(avtale.getGjeldendeInnhold().getDeltakerFornavn()).isNull();
            softly.assertThat(avtale.getGjeldendeInnhold().getDeltakerEtternavn()).isNull();
            softly.assertThat(avtale.getGjeldendeInnhold().getBedriftNavn()).isNull();
            softly.assertThat(avtale.getBedriftNr()).isEqualTo(bedriftNr);
            softly.assertThat(avtale.getGjeldendeInnhold().getArbeidsgiverFornavn()).isNull();
            softly.assertThat(avtale.getGjeldendeInnhold().getArbeidsgiverEtternavn()).isNull();
            softly.assertThat(avtale.getGjeldendeInnhold().getArbeidsgiverTlf()).isNull();
            softly.assertThat(avtale.getGjeldendeInnhold().getVeilederFornavn()).isNull();
            softly.assertThat(avtale.getGjeldendeInnhold().getVeilederEtternavn()).isNull();
            softly.assertThat(avtale.getGjeldendeInnhold().getVeilederTlf()).isNull();
            softly.assertThat(avtale.getGjeldendeInnhold().getOppfolging()).isNull();
            softly.assertThat(avtale.getGjeldendeInnhold().getTilrettelegging()).isNull();
            softly.assertThat(avtale.getGjeldendeInnhold().getStartDato()).isNull();
            softly.assertThat(avtale.getGjeldendeInnhold().getSluttDato()).isNull();
            softly.assertThat(avtale.getGjeldendeInnhold().getStillingprosent()).isNull();
            softly.assertThat(avtale.getGjeldendeInnhold().getStillingstittel()).isNull();
            softly.assertThat(avtale.erGodkjentAvDeltaker()).isFalse();
            softly.assertThat(avtale.erGodkjentAvArbeidsgiver()).isFalse();
            softly.assertThat(avtale.erGodkjentAvVeileder()).isFalse();
        });
    }

    @Test
    public void nyAvtaleSkalFeileHvisManglerDeltaker() {
        assertThatThrownBy(() -> Avtale.opprett(new OpprettAvtale(null, new BedriftNr("111222333"), Tiltakstype.ARBEIDSTRENING), Avtaleopphav.VEILEDER, new NavIdent("X123456"))).isInstanceOf(TiltaksgjennomforingException.class);
    }

    @Test
    public void nyAvtaleSkalFeileHvisManglerArbeidsgiver() {
        assertThatThrownBy(() -> Avtale.opprett(new OpprettAvtale(new Fnr("23078637692"), null, Tiltakstype.ARBEIDSTRENING), Avtaleopphav.VEILEDER, new NavIdent("X123456"))).isInstanceOf(TiltaksgjennomforingException.class);
    }

    @Test
    public void nyAvtaleSkalFeileHvisManglerVeileder() {
        assertThatThrownBy(() -> Avtale.opprett(new OpprettAvtale(new Fnr("23078637692"), new BedriftNr("000111222"), Tiltakstype.ARBEIDSTRENING), Avtaleopphav.VEILEDER, null)).isInstanceOf(TiltaksgjennomforingException.class);
    }

    @Test
    public void nyAvtaleSkalFeileHvisDeltakerErForUng() {
        assertFeilkode(Feilkode.SOMMERJOBB_IKKE_GAMMEL_NOK, () -> Avtale.opprett(new OpprettAvtale(new Fnr("24011070772"), new BedriftNr("000111222"), Tiltakstype.ARBEIDSTRENING), Avtaleopphav.VEILEDER, null));
    }

    @Test
    public void nyAvtaleSkalFeileHvisDeltakerErForGammelForSommerjobb() {
        assertFeilkode(Feilkode.SOMMERJOBB_FOR_GAMMEL, () -> Avtale.opprett(new OpprettAvtale(new Fnr("08098114468"), new BedriftNr("000111222"), Tiltakstype.SOMMERJOBB), Avtaleopphav.VEILEDER, null));
    }

    @Test
    public void sjekkVersjon__ugyldig_versjon() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        assertThatThrownBy(() -> avtale.sjekkSistEndret(Instant.MIN)).isInstanceOf(SamtidigeEndringerException.class);
    }

    @Test
    public void sjekkVersjon__null() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        assertThatThrownBy(() -> avtale.sjekkSistEndret(null)).isInstanceOf(SamtidigeEndringerException.class);
    }

    @Test
    public void sjekkVersjon__gyldig_versjon() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        avtale.sjekkSistEndret(Now.instant());
    }

    @Test
    public void endreAvtaleSkalOppdatereRiktigeFelt() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        EndreAvtale endreAvtale = TestData.endringPåAlleArbeidstreningFelter();
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(avtale.getGjeldendeInnhold().getDeltakerFornavn()).isEqualTo(endreAvtale.getDeltakerFornavn());
            softly.assertThat(avtale.getGjeldendeInnhold().getDeltakerEtternavn()).isEqualTo(endreAvtale.getDeltakerEtternavn());
            softly.assertThat(avtale.getGjeldendeInnhold().getDeltakerTlf()).isEqualTo(endreAvtale.getDeltakerTlf());
            softly.assertThat(avtale.getGjeldendeInnhold().getBedriftNavn()).isEqualTo(endreAvtale.getBedriftNavn());
            softly.assertThat(avtale.getGjeldendeInnhold().getArbeidsgiverFornavn()).isEqualTo(endreAvtale.getArbeidsgiverFornavn());
            softly.assertThat(avtale.getGjeldendeInnhold().getArbeidsgiverEtternavn()).isEqualTo(endreAvtale.getArbeidsgiverEtternavn());
            softly.assertThat(avtale.getGjeldendeInnhold().getArbeidsgiverTlf()).isEqualTo(endreAvtale.getArbeidsgiverTlf());
            softly.assertThat(avtale.getGjeldendeInnhold().getVeilederFornavn()).isEqualTo(endreAvtale.getVeilederFornavn());
            softly.assertThat(avtale.getGjeldendeInnhold().getVeilederEtternavn()).isEqualTo(endreAvtale.getVeilederEtternavn());
            softly.assertThat(avtale.getGjeldendeInnhold().getVeilederTlf()).isEqualTo(endreAvtale.getVeilederTlf());
            softly.assertThat(avtale.getGjeldendeInnhold().getOppfolging()).isEqualTo(endreAvtale.getOppfolging());
            softly.assertThat(avtale.getGjeldendeInnhold().getTilrettelegging()).isEqualTo(endreAvtale.getTilrettelegging());
            softly.assertThat(avtale.getGjeldendeInnhold().getStartDato()).isEqualTo(endreAvtale.getStartDato());
            softly.assertThat(avtale.getGjeldendeInnhold().getSluttDato()).isEqualTo(endreAvtale.getSluttDato());
            softly.assertThat(avtale.getGjeldendeInnhold().getStillingprosent()).isEqualTo(endreAvtale.getStillingprosent());
            softly.assertThat(avtale.getGjeldendeInnhold().getMaal()).isEqualTo(endreAvtale.getMaal());
            softly.assertThat(avtale.getGjeldendeInnhold().getStillingstittel()).isEqualTo(endreAvtale.getStillingstittel());
        });
    }

    @Test
    public void endreAvtale__for_langt_maal_skal_feile() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        Maal etMaal = TestData.etMaal();
        etMaal.setBeskrivelse("Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.");
        EndreAvtale endreAvtale = new EndreAvtale();
        endreAvtale.setMaal(List.of(etMaal));
        assertThatThrownBy(() -> avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER)).isInstanceOf(TiltaksgjennomforingException.class);
    }

    @Test
    public void endreAvtale__startdato_satt_men_ikke_sluttdato() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        EndreAvtale endreAvtale = new EndreAvtale();
        LocalDate startDato = Now.localDate();
        endreAvtale.setStartDato(startDato);
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        assertThat(avtale.getGjeldendeInnhold().getStartDato()).isEqualTo(startDato);
    }

    @Test
    public void endreAvtale__sluttdato_satt_men_ikke_startdato() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        EndreAvtale endreAvtale = new EndreAvtale();
        LocalDate sluttDato = Now.localDate();
        endreAvtale.setSluttDato(sluttDato);
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        assertThat(avtale.getGjeldendeInnhold().getSluttDato()).isEqualTo(sluttDato);
    }

    @Test
    public void endreAvtale__startdato_og_sluttdato_satt_18mnd() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        EndreAvtale endreAvtale = new EndreAvtale();
        LocalDate startDato = Now.localDate();
        LocalDate sluttDato = startDato.plusMonths(18).minusDays(1);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        assertThat(avtale.getGjeldendeInnhold().getStartDato()).isEqualTo(startDato);
        assertThat(avtale.getGjeldendeInnhold().getSluttDato()).isEqualTo(sluttDato);
    }

    @Test
    public void endreAvtale__startdato_og_sluttdato_satt_over_18mnd() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        EndreAvtale endreAvtale = new EndreAvtale();
        LocalDate startDato = Now.localDate();
        LocalDate sluttDato = startDato.plusMonths(18);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        assertThatThrownBy(() -> avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER)).isInstanceOf(VarighetForLangArbeidstreningException.class);
    }

    @Test
    public void endreAvtale__startdato_er_etter_sluttdato() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        EndreAvtale endreAvtale = new EndreAvtale();
        LocalDate startDato = Now.localDate();
        LocalDate sluttDato = startDato.minusDays(1);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        assertFeilkode(Feilkode.START_ETTER_SLUTT, () -> avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER));
    }

    @Test
    public void endreAvtaleSkalIkkeEndreGodkjenninger() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        boolean deltakerGodkjenningFoerEndring = avtale.erGodkjentAvDeltaker();
        boolean arbeidsgiverGodkjenningFoerEndring = avtale.erGodkjentAvArbeidsgiver();
        boolean veilederGodkjenningFoerEndring = avtale.erGodkjentAvVeileder();

        avtale.endreAvtale(TestData.endringPåAlleArbeidstreningFelter(), Avtalerolle.VEILEDER);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(deltakerGodkjenningFoerEndring).isEqualTo(avtale.erGodkjentAvDeltaker());
            softly.assertThat(arbeidsgiverGodkjenningFoerEndring).isEqualTo(avtale.erGodkjentAvArbeidsgiver());
            softly.assertThat(veilederGodkjenningFoerEndring).isEqualTo(avtale.erGodkjentAvVeileder());
        });
    }

    @Test
    public void endreAvtaleSkalInkrementereVersjon() throws InterruptedException {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        Instant førstEndret = avtale.getSistEndret();
        Thread.sleep(10);
        avtale.endreAvtale(TestData.ingenEndring(), Avtalerolle.VEILEDER);
        assertThat(avtale.getSistEndret()).isAfter(førstEndret);
    }

    @Test
    public void kanIkkeGodkjennesNårNoeMangler__arbeidstrening() {
        Set<String> arbeidstreningsfelter = Set.of(
                AvtaleInnhold.Fields.deltakerFornavn,
                AvtaleInnhold.Fields.deltakerEtternavn,
                AvtaleInnhold.Fields.deltakerTlf,
                AvtaleInnhold.Fields.bedriftNavn,
                AvtaleInnhold.Fields.arbeidsgiverFornavn,
                AvtaleInnhold.Fields.arbeidsgiverEtternavn,
                AvtaleInnhold.Fields.arbeidsgiverTlf,
                AvtaleInnhold.Fields.veilederFornavn,
                AvtaleInnhold.Fields.veilederEtternavn,
                AvtaleInnhold.Fields.veilederTlf,
                AvtaleInnhold.Fields.stillingstittel,
                AvtaleInnhold.Fields.arbeidsoppgaver,
                AvtaleInnhold.Fields.stillingprosent,
                AvtaleInnhold.Fields.antallDagerPerUke,
                AvtaleInnhold.Fields.maal,
                AvtaleInnhold.Fields.startDato,
                AvtaleInnhold.Fields.sluttDato,
                AvtaleInnhold.Fields.tilrettelegging,
                AvtaleInnhold.Fields.oppfolging
        );

        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.ARBEIDSTRENING), Avtaleopphav.VEILEDER, TestData.enNavIdent());

        testAtAlleFelterMangler(avtale, arbeidstreningsfelter);
        testAtHvertEnkeltFeltMangler(avtale, arbeidstreningsfelter, avtale.getTiltakstype());
    }

    @Test
    public void kanIkkeGodkjennesNårNoeMangler__midlertidig_lønnstilskudd() {
        Set<String> lønnstilskuddfelter = Set.of(
                AvtaleInnhold.Fields.deltakerFornavn,
                AvtaleInnhold.Fields.deltakerEtternavn,
                AvtaleInnhold.Fields.deltakerTlf,
                AvtaleInnhold.Fields.bedriftNavn,
                AvtaleInnhold.Fields.arbeidsgiverFornavn,
                AvtaleInnhold.Fields.arbeidsgiverEtternavn,
                AvtaleInnhold.Fields.arbeidsgiverTlf,
                AvtaleInnhold.Fields.veilederFornavn,
                AvtaleInnhold.Fields.veilederEtternavn,
                AvtaleInnhold.Fields.veilederTlf,
                AvtaleInnhold.Fields.stillingstittel,
                AvtaleInnhold.Fields.arbeidsoppgaver,
                AvtaleInnhold.Fields.stillingprosent,
                AvtaleInnhold.Fields.antallDagerPerUke,
                AvtaleInnhold.Fields.stillingstype,
                AvtaleInnhold.Fields.startDato,
                AvtaleInnhold.Fields.sluttDato,
                AvtaleInnhold.Fields.arbeidsgiverKontonummer,
                AvtaleInnhold.Fields.manedslonn,
                AvtaleInnhold.Fields.feriepengesats,
                AvtaleInnhold.Fields.otpSats,
                AvtaleInnhold.Fields.arbeidsgiveravgift,
                AvtaleInnhold.Fields.tilrettelegging,
                AvtaleInnhold.Fields.oppfolging,
                AvtaleInnhold.Fields.harFamilietilknytning
        );

        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD), Avtaleopphav.VEILEDER, TestData.enNavIdent());

        testAtAlleFelterMangler(avtale, lønnstilskuddfelter);
        testAtHvertEnkeltFeltMangler(avtale, lønnstilskuddfelter, avtale.getTiltakstype());
    }

    @Test
    public void kanIkkeGodkjennesNårNoeMangler__varig_lønnstilskudd() {
        Set<String> lønnstilskuddfelter = Set.of(
                AvtaleInnhold.Fields.deltakerFornavn,
                AvtaleInnhold.Fields.deltakerEtternavn,
                AvtaleInnhold.Fields.deltakerTlf,
                AvtaleInnhold.Fields.bedriftNavn,
                AvtaleInnhold.Fields.arbeidsgiverFornavn,
                AvtaleInnhold.Fields.arbeidsgiverEtternavn,
                AvtaleInnhold.Fields.arbeidsgiverTlf,
                AvtaleInnhold.Fields.veilederFornavn,
                AvtaleInnhold.Fields.veilederEtternavn,
                AvtaleInnhold.Fields.veilederTlf,
                AvtaleInnhold.Fields.stillingstittel,
                AvtaleInnhold.Fields.arbeidsoppgaver,
                AvtaleInnhold.Fields.stillingprosent,
                AvtaleInnhold.Fields.antallDagerPerUke,
                AvtaleInnhold.Fields.stillingstype,
                AvtaleInnhold.Fields.startDato,
                AvtaleInnhold.Fields.sluttDato,
                AvtaleInnhold.Fields.arbeidsgiverKontonummer,
                AvtaleInnhold.Fields.manedslonn,
                AvtaleInnhold.Fields.feriepengesats,
                AvtaleInnhold.Fields.otpSats,
                AvtaleInnhold.Fields.arbeidsgiveravgift,
                AvtaleInnhold.Fields.tilrettelegging,
                AvtaleInnhold.Fields.oppfolging,
                AvtaleInnhold.Fields.harFamilietilknytning,
                AvtaleInnhold.Fields.lonnstilskuddProsent,
                Fields.refusjonKontaktpersonFornavn
        );

        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.VARIG_LONNSTILSKUDD), Avtaleopphav.VEILEDER, TestData.enNavIdent());
        avtale.getGjeldendeInnhold().setRefusjonKontaktperson(new RefusjonKontaktperson(null, "Duck", "12345678", true));
        testAtAlleFelterMangler(avtale, lønnstilskuddfelter);
        testAtHvertEnkeltFeltMangler(avtale, lønnstilskuddfelter, avtale.getTiltakstype());
    }

    @Test
    public void kanIkkeGodkjennesNårNoeMangler__mentor() {
        Set<String> mentorfelter = Set.of(
                AvtaleInnhold.Fields.deltakerFornavn,
                AvtaleInnhold.Fields.deltakerEtternavn,
                AvtaleInnhold.Fields.deltakerTlf,
                AvtaleInnhold.Fields.bedriftNavn,
                AvtaleInnhold.Fields.arbeidsgiverFornavn,
                AvtaleInnhold.Fields.arbeidsgiverEtternavn,
                AvtaleInnhold.Fields.arbeidsgiverTlf,
                AvtaleInnhold.Fields.veilederFornavn,
                AvtaleInnhold.Fields.veilederEtternavn,
                AvtaleInnhold.Fields.veilederTlf,
                AvtaleInnhold.Fields.startDato,
                AvtaleInnhold.Fields.sluttDato,
                AvtaleInnhold.Fields.mentorFornavn,
                AvtaleInnhold.Fields.mentorEtternavn,
                AvtaleInnhold.Fields.mentorTimelonn,
                AvtaleInnhold.Fields.mentorOppgaver,
                AvtaleInnhold.Fields.mentorAntallTimer,
                AvtaleInnhold.Fields.tilrettelegging,
                AvtaleInnhold.Fields.oppfolging,
                AvtaleInnhold.Fields.mentorTlf,
                AvtaleInnhold.Fields.harFamilietilknytning
        );

        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MENTOR), Avtaleopphav.VEILEDER, TestData.enNavIdent());

        testAtAlleFelterMangler(avtale, mentorfelter);
        testAtHvertEnkeltFeltMangler(avtale, mentorfelter, avtale.getTiltakstype());
    }

    private static void testAtHvertEnkeltFeltMangler(Avtale avtale, Set<String> felterSomKrevesForTiltakstype, Tiltakstype tiltakstype) {
        for (String felt : felterSomKrevesForTiltakstype) {
            EndreAvtale endreAvtale = endringPåAltUtenom(felt, tiltakstype);
            avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
            assertThat(avtale.felterSomIkkeErFyltUt()).containsOnly(felt);
            assertFeilkode(Feilkode.ALT_MA_VAERE_FYLT_UT, () -> avtale.godkjennForArbeidsgiver(TestData.enIdentifikator()));
        }
    }

    private static void testAtAlleFelterMangler(Avtale avtale, Set<String> arbeidstreningsfelter) {
        assertThat(avtale.felterSomIkkeErFyltUt()).containsExactlyInAnyOrderElementsOf(arbeidstreningsfelter);
    }

    private static EndreAvtale endringPåAltUtenom(String felt, Tiltakstype tiltakstype) {
        EndreAvtale endreAvtale =
                switch (tiltakstype) {
                    case ARBEIDSTRENING -> TestData.endringPåAlleArbeidstreningFelter();
                    case MENTOR -> TestData.endringPåAlleMentorFelter();
                    case INKLUDERINGSTILSKUDD -> TestData.endringPåAlleInkluderingstilskuddFelter();
                    case VTAO -> TestData.endringPåAlleVTAOFelter();
                    case MIDLERTIDIG_LONNSTILSKUDD, SOMMERJOBB, VARIG_LONNSTILSKUDD ->
                            TestData.endringPåAlleLønnstilskuddFelter();
                };
        Object field = ReflectionTestUtils.getField(endreAvtale, felt);
        if (field instanceof Collection) {
            ((Collection) field).clear();
        } else {
            ReflectionTestUtils.setField(endreAvtale, felt, null);
        }
        return endreAvtale;
    }

    @Test
    public void kanGodkjennesNaarAltErUtfylt() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.sjekkOmAltErKlarTilGodkjenning();
    }

    @Test
    public void kan_ikke_godkjennes_når_alt_er_utfylt_men_beregning_mangler() {
        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD), Avtaleopphav.VEILEDER, TestData.enNavIdent());
        avtale.setEnhetOppfolging("0000");
        avtale.setEnhetsnavnOppfolging("0000");
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS);
        avtale.endreAvtale(TestData.endringPåAlleLønnstilskuddFelter(), Avtalerolle.VEILEDER);

        // Later som at det har skjedd noe mystisk med prosenten, kan skyldes feil ved innhenting fra Arena
        avtale.getGjeldendeInnhold().setLonnstilskuddProsent(null);
        assertFeilkode(Feilkode.MANGLER_BEREGNING, () -> avtale.sjekkOmAltErKlarTilGodkjenning());

        // Later som at det har skjedd noe mystisk med sum, kan skyldes feil ved innhenting fra Arena
        avtale.getGjeldendeInnhold().setLonnstilskuddProsent(67);
        avtale.getGjeldendeInnhold().setSumLonnstilskudd(null);
        assertFeilkode(Feilkode.MANGLER_BEREGNING, () -> avtale.sjekkOmAltErKlarTilGodkjenning());
    }

    @Test
    public void status__ny_avtale() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        assertThat(avtale.getStatus()).isEqualTo(Status.PÅBEGYNT);
    }

    @Test
    public void status__null_startdato() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        avtale.getGjeldendeInnhold().setStartDato(null);
        avtale.getGjeldendeInnhold().setSluttDato(null);
        assertThat(avtale.getStatus()).isEqualTo(Status.PÅBEGYNT);
    }

    @Test
    public void status__noe_fylt_ut() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        avtale.getGjeldendeInnhold().setStartDato(Now.localDate().plusDays(5));
        avtale.getGjeldendeInnhold().setSluttDato(avtale.getGjeldendeInnhold().getStartDato().plusMonths(3));
        avtale.getGjeldendeInnhold().setBedriftNavn("testbedriftsnavn");
        avtale.setStatus(Status.fra(avtale));
        assertThat(avtale.getStatus()).isEqualTo(Status.PÅBEGYNT);
    }

    @Test
    public void status__avsluttet_i_gaar() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setStartDato(Now.localDate().minusWeeks(4).minusDays(1));
        avtale.getGjeldendeInnhold().setSluttDato(avtale.getGjeldendeInnhold().getStartDato().plusWeeks(4));
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setAvtaleInngått(Now.localDateTime());
        avtale.setStatus(Status.fra(avtale));
        assertThat(avtale.getStatus()).isEqualTo(Status.AVSLUTTET);
    }

    @Test
    public void status__avslutter_i_dag() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setStartDato(Now.localDate().minusWeeks(4));
        avtale.getGjeldendeInnhold().setSluttDato(avtale.getGjeldendeInnhold().getStartDato().plusWeeks(4));
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setAvtaleInngått(Now.localDateTime());
        avtale.setStatus(Status.fra(avtale));
        assertThat(avtale.getStatus()).isEqualTo(Status.GJENNOMFØRES);
    }

    @Test
    public void status__startet_i_dag() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setStartDato(Now.localDate());
        avtale.getGjeldendeInnhold().setSluttDato(avtale.getGjeldendeInnhold().getStartDato().plusWeeks(4));
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setAvtaleInngått(Now.localDateTime());
        avtale.setStatus(Status.fra(avtale));
        assertThat(avtale.getStatus()).isEqualTo(Status.GJENNOMFØRES);
    }

    @Test
    public void status__starter_i_morgen() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setStartDato(Now.localDate().plusDays(1));
        avtale.getGjeldendeInnhold().setSluttDato(avtale.getGjeldendeInnhold().getStartDato().plusWeeks(4));
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setAvtaleInngått(Now.localDateTime());
        avtale.setStatus(Status.fra(avtale));
        assertThat(avtale.getStatus()).isEqualTo(Status.KLAR_FOR_OPPSTART);
    }

    @Test
    public void status__klar_for_godkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setStatus(Status.fra(avtale));
        assertThat(avtale.getStatus()).isEqualTo(Status.MANGLER_GODKJENNING);
    }

    @Test
    public void status__veileder_har_godkjent() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setStartDato(Now.localDate().plusDays(1));
        avtale.getGjeldendeInnhold().setSluttDato(Now.localDate().plusDays(1).plusMonths(1).minusDays(1));
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setAvtaleInngått(Now.localDateTime());
        avtale.setStatus(Status.fra(avtale));
        assertThat(avtale.getStatus()).isEqualTo(Status.KLAR_FOR_OPPSTART);
    }

    @Test
    public void status__naar_deltaker_tlf_mangler() {
        // Deltaker tlf ble innført etter at avtaler er opprettet. Det kan derfor være
        // avtaler som er inngått som mangler tlf.
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setStartDato(Now.localDate().minusDays(1));
        avtale.getGjeldendeInnhold().setSluttDato(Now.localDate().minusDays(1).plusMonths(1));
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setAvtaleInngått(Now.localDateTime());
        avtale.getGjeldendeInnhold().setDeltakerTlf(null);
        avtale.setStatus(Status.fra(avtale));
        assertThat(avtale.getStatus()).isEqualTo(Status.GJENNOMFØRES);
    }

    @Test
    public void status__annullert() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        avtale.annuller(TestData.enVeileder(avtale), "grunnen");
        assertThat(avtale.getStatus()).isEqualTo(Status.ANNULLERT);
        assertThat(avtale.getAnnullertTidspunkt()).isNotNull();
        assertThat(avtale.getAnnullertGrunn()).isEqualTo("grunnen");
    }

    @Test
    public void avbryt_ufordelt_avtale_skal_bli_fordelt() {
        Avtale avtale = TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordelt();
        Veileder veileder = TestData.enVeileder(new NavIdent("Z123456"));
        avtale.annuller(veileder, "grunnen");

        assertThat(avtale.getStatus()).isEqualTo(Status.ANNULLERT);
        assertThat(avtale.erUfordelt()).isFalse();
        assertThat(avtale.getVeilederNavIdent()).isEqualTo(veileder.getIdentifikator());
    }

    @Test
    public void tom_avtale_kan_avbrytes() {
        Avtale tomAvtale = TestData.enArbeidstreningAvtale();
        assertThat(tomAvtale.kanAvbrytes()).isTrue();
    }

    @Test
    public void fylt_avtale_kan_avbrytes() {
        Avtale fyltAvtale = TestData.enAvtaleMedAltUtfylt();
        assertThat(fyltAvtale.kanAvbrytes()).isTrue();
    }

    @Test
    public void godkjent_av_veileder_avtale_kan_avbrytes() {
        Avtale godkjentAvtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        assertThat(godkjentAvtale.kanAvbrytes()).isTrue();
    }

    @Test
    public void allerede_avbrutt_avtale_kan_ikke_avbrytes() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setAvbrutt(true);
        assertThat(avtale.kanAvbrytes()).isFalse();
    }

    @Test
    public void utforEndring__kalles_ved_endreAvtale() throws InterruptedException {
        Instant førEndringen = Now.instant();
        Avtale avtale = TestData.enArbeidstreningAvtale();
        Thread.sleep(10);
        avtale.endreAvtale(TestData.ingenEndring(), Avtalerolle.VEILEDER);
        assertThat(avtale.getSistEndret()).isAfter(førEndringen);
    }

    @Test
    public void utforEndring__kalles_ved_godkjennForDeltaker() throws InterruptedException {
        Instant førEndringen = Now.instant();
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Thread.sleep(10);
        avtale.godkjennForDeltaker(TestData.enIdentifikator());
        assertThat(avtale.getSistEndret()).isAfter(førEndringen);
    }

    @Test
    public void utforEndring__kalles_ved_godkjennForArbeidsgiver() throws InterruptedException {
        Instant førEndringen = Now.instant();
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Thread.sleep(10);
        avtale.godkjennForArbeidsgiver(TestData.enIdentifikator());
        assertThat(avtale.getSistEndret()).isAfter(førEndringen);
    }

    @Test
    public void utforEndring__kalles_ved_godkjennForVeileder() throws InterruptedException {
        Instant førEndringen = Now.instant();
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Thread.sleep(10);
        avtale.godkjennForArbeidsgiver(TestData.enIdentifikator());
        assertThat(avtale.getSistEndret()).isAfter(førEndringen);
    }

    @Test
    public void utforEndring__kalles_ved_godkjennForVeilederOgDeltaker() throws InterruptedException {
        Instant førEndringen = Now.instant();
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        Thread.sleep(10);
        avtale.godkjennForVeilederOgDeltaker(TestData.enNavIdent(), TestData.enGodkjentPaVegneGrunn());
        assertThat(avtale.getSistEndret()).isAfter(førEndringen);
    }

    @Test
    public void utforEndring__kalles_ved_opphevGodkjenningerSomArbeidsgiver() throws InterruptedException {
        Instant førEndringen = Now.instant();
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Thread.sleep(10);
        avtale.opphevGodkjenningerSomArbeidsgiver();
        assertThat(avtale.getSistEndret()).isAfter(førEndringen);
    }

    @Test
    public void utforEndring__kalles_ved_opphevGodkjenningerSomVeileder() throws InterruptedException {
        Instant førEndringen = Now.instant();
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Thread.sleep(10);
        avtale.opphevGodkjenningerSomVeileder();
        assertThat(avtale.getSistEndret()).isAfter(førEndringen);
    }

    @Test
    public void utforEndring__kalles_ved_annuller() throws InterruptedException {
        Instant førEndringen = Now.instant();
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Thread.sleep(10);
        avtale.annuller(TestData.enVeileder(avtale), "grunn");
        assertThat(avtale.getSistEndret()).isAfter(førEndringen);
    }

    @Test
    public void utforEndring__kalles_ved_endreOppfølgingOgTilrettelegging() throws InterruptedException {
        Instant førEndringen = Now.instant();
        Avtale avtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        Thread.sleep(10);
        avtale.endreOppfølgingOgTilrettelegging(new EndreOppfølgingOgTilrettelegging("Oppfølging", "Tilrettelegging"), TestData.enNavIdent());
        assertThat(avtale.getSistEndret()).isAfter(førEndringen);
    }

    @Test
    public void utforEndring__kalles_ved_forlengAvtale() {
        Avtale avtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        Instant sistEndret = avtale.getSistEndret();
        avtale.forlengAvtale(avtale.getGjeldendeInnhold().getSluttDato().plusDays(1), TestData.enNavIdent());
        assertThat(avtale.getSistEndret()).isAfter(sistEndret);
    }

    @Test
    public void utforEndring__kalles_ved_forkortAvtale() {
        Avtale avtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        Instant sistEndret = avtale.getSistEndret();
        avtale.forkortAvtale(avtale.getGjeldendeInnhold().getSluttDato().minusDays(1), ForkortetGrunn.av("lala", null), TestData.enNavIdent());
        assertThat(avtale.getSistEndret()).isAfter(sistEndret);
    }

    @Test
    public void avtaleklarForOppstart() {
        Avtale avtale = TestData.enAvtaleKlarForOppstart();
        avtale.setStatus(Status.fra(avtale));
        assertThat(avtale.getStatus()).isEqualTo(Status.KLAR_FOR_OPPSTART);
    }

    @Test
    public void avtale_opprettet_av_arbedsgiver_skal_være_ufordelt() {
        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD), Avtaleopphav.ARBEIDSGIVER);
        assertThat(avtale.getOpphav()).isEqualTo(Avtaleopphav.ARBEIDSGIVER);
        assertThat(avtale.erUfordelt()).isTrue();
    }

    @Test
    public void avtale_kan_være_ufordelt_selv_om_alt_er_utfylt() {
        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD), Avtaleopphav.ARBEIDSGIVER);
        avtale.endreAvtale(TestData.endringPåAlleLønnstilskuddFelter(), Avtalerolle.ARBEIDSGIVER);
        assertThat(avtale.erUfordelt()).isTrue();
    }

    @Test
    public void avtale_skal_ikke_kunne_godkjennes_uten_navident() {
        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD), Avtaleopphav.ARBEIDSGIVER);
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS);
        avtale.endreAvtale(TestData.endringPåAlleLønnstilskuddFelter(), Avtalerolle.ARBEIDSGIVER);
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        assertFeilkode(Feilkode.MANGLER_VEILEDER_PÅ_AVTALE, () -> arbeidsgiver.godkjennAvtale(avtale));
    }

    @Test
    public void ufordelt_avtale_ikke_klar_for_godkjenning() {
        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.ARBEIDSTRENING), Avtaleopphav.ARBEIDSGIVER);
        avtale.endreAvtale(TestData.endringPåAlleArbeidstreningFelter(), Avtalerolle.ARBEIDSGIVER);
        assertFeilkode(Feilkode.MANGLER_VEILEDER_PÅ_AVTALE, () -> avtale.sjekkOmAltErKlarTilGodkjenning());
    }

    @Test
    public void ufordelt_avtale_må_tildeles_veileder_før_den_kan_godkjennes() {
        Avtale avtale = Avtale.opprett(
                new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD),
                Avtaleopphav.ARBEIDSGIVER
        );

        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        PersondataService persondataService = mock(PersondataService.class);

        Veileder veileder = new Veileder(
            TestData.enNavIdent(),
            null,
            tilgangskontrollService,
            persondataService,
            mock(Norg2Client.class),
            Set.of(new NavEnhet("4802", "Trysil")),
            mock(SlettemerkeProperties.class),
            TestData.INGEN_AD_GRUPPER,
            mock(VeilarboppfolgingService.class),
            mock(FeatureToggleService.class),
            mock(EregService.class)
        );
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS);
        avtale.endreAvtale(TestData.endringPåAlleLønnstilskuddFelter(), Avtalerolle.ARBEIDSGIVER);
        assertFeilkode(Feilkode.MANGLER_VEILEDER_PÅ_AVTALE, () -> avtale.sjekkOmAltErKlarTilGodkjenning());
        when(tilgangskontrollService.hentSkrivetilgang(veileder, avtale.getDeltakerFnr())).thenReturn(new Tilgang.Tillat());

        veileder.overtaAvtale(avtale);
        avtale.godkjennForArbeidsgiver(TestData.enIdentifikator());
        assertThat(avtale.erGodkjentAvArbeidsgiver()).isTrue();
    }


    @Test
    public void ufordelt_midlertidig_lts_avtale_endrer_avtale_med_lavere_lønnstilskuddprosent_enn_mellom_kun_40_60_prosent() {
        Avtale avtale = Avtale.opprett(
                new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD),
                Avtaleopphav.VEILEDER,
                new NavIdent("Z123456"));
        avtale.setKvalifiseringsgruppe(null);
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        endreAvtale.setLonnstilskuddProsent(20);
        assertThatThrownBy(() -> avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER))
                .isInstanceOf(FeilLonnstilskuddsprosentException.class);
    }

    @Test
    public void ufordelt_midlertidig_lts_avtale_endrer_avtale_med_høyere_enn_maks_lønnstilskuddprosent_enn_60_prosent() {
        Avtale avtale = Avtale.opprett(
                new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD),
                Avtaleopphav.VEILEDER,
                new NavIdent("Z123456")
        );
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        avtale.setKvalifiseringsgruppe(null);
        endreAvtale.setLonnstilskuddProsent(67);
        assertThatThrownBy(() -> avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER))
                .isInstanceOf(FeilLonnstilskuddsprosentException.class);
    }


    @Test
    public void ufordelt_varig_lts_avtale_endrer_avtale_med_lavere_lønnstilskuddprosent_enn_0_prosent() {
        Avtale avtale = Avtale.opprett(
                new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.VARIG_LONNSTILSKUDD),
                Avtaleopphav.VEILEDER,
                new NavIdent("Z123456")
        );
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        endreAvtale.setLonnstilskuddProsent(-1);
        assertThatThrownBy(() -> avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER))
                .isInstanceOf(FeilLonnstilskuddsprosentException.class);
    }

    @Test
    public void ufordelt_varig_lts_avtale_endrer_avtale_med_høyere_enn_maks_lønnstilskuddprosent_enn_75_prosent() {
        Avtale avtale = Avtale.opprett(
                new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.VARIG_LONNSTILSKUDD),
                Avtaleopphav.VEILEDER,
                new NavIdent("Z123456")
        );
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        endreAvtale.setLonnstilskuddProsent(100);
        assertThatThrownBy(() -> avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER))
                .isInstanceOf(FeilLonnstilskuddsprosentException.class);
    }

    @Test
    public void endre_stillingsbeskrivelse_setter_riktige_felter() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleGodkjentAvVeileder();
        String stillingstittel = "Kokk";
        String arbeidsoppgaver = "Lage mat";
        Integer stillingStyrk08 = 1234;
        Integer stillingKonseptId = 9999;
        BigDecimal stillingprosent = BigDecimal.valueOf(90.0);
        BigDecimal antallDagerPerUke = BigDecimal.valueOf(4.0);
        var endreStillingsbeskrivelse = new EndreStillingsbeskrivelse(stillingstittel, arbeidsoppgaver, stillingStyrk08, stillingKonseptId, stillingprosent, antallDagerPerUke);

        assertThat(avtale.getGjeldendeInnhold().getVersjon()).isEqualTo(1);
        avtale.endreStillingsbeskrivelse(endreStillingsbeskrivelse, new NavIdent("Z123456"));

        assertThat(avtale.getGjeldendeInnhold().getVersjon()).isEqualTo(2);
        assertThat(avtale.getGjeldendeInnhold().getStillingstittel()).isEqualTo(stillingstittel);
        assertThat(avtale.getGjeldendeInnhold().getArbeidsoppgaver()).isEqualTo(arbeidsoppgaver);
        assertThat(avtale.getGjeldendeInnhold().getStillingStyrk08()).isEqualTo(stillingStyrk08);
        assertThat(avtale.getGjeldendeInnhold().getStillingKonseptId()).isEqualTo(stillingKonseptId);
        assertThat(avtale.getGjeldendeInnhold().getStillingprosent()).isEqualTo(stillingprosent);
        assertThat(avtale.getGjeldendeInnhold().getAntallDagerPerUke()).isEqualTo(antallDagerPerUke);
    }

    @Test
    public void endre_tilskuddsberegning_kun_inngått_avtale() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        assertFeilkode(Feilkode.KAN_IKKE_ENDRE_OKONOMI_IKKE_GODKJENT_AVTALE, () -> avtale.endreTilskuddsberegning(TestData.enEndreTilskuddsberegning(), TestData.enNavIdent()));
    }

    @Test
    public void endre_tilskuddsberegning_ugyldig_input() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleGodkjentAvVeileder();
        assertFeilkode(Feilkode.KAN_IKKE_ENDRE_OKONOMI_UGYLDIG_INPUT, () -> avtale.endreTilskuddsberegning(TestData.enEndreTilskuddsberegning().toBuilder().manedslonn(null).build(), TestData.enNavIdent()));
    }

    @Test
    public void forleng_setter_riktige_felter() {
        Avtale avtale = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        LocalDate nySluttDato = avtale.getGjeldendeInnhold().getSluttDato().plusMonths(1);

        assertThat(avtale.getGjeldendeInnhold().getVersjon()).isEqualTo(1);
        avtale.forlengAvtale(nySluttDato, TestData.enNavIdent());

        assertThat(avtale.getGjeldendeInnhold().getVersjon()).isEqualTo(2);
        assertThat(avtale.erGodkjentAvVeileder()).isTrue();
        assertThat(avtale.getGjeldendeInnhold().getSluttDato()).isEqualTo(nySluttDato);
    }

    @Test
    public void forkort_setter_riktige_felter() {
        Avtale avtale = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        LocalDate nySluttDato = avtale.getGjeldendeInnhold().getSluttDato().minusDays(1);

        assertThat(avtale.getGjeldendeInnhold().getVersjon()).isEqualTo(1);
        avtale.forkortAvtale(nySluttDato, ForkortetGrunn.av("grunn", ""), TestData.enNavIdent());

        assertThat(avtale.getGjeldendeInnhold().getVersjon()).isEqualTo(2);
        assertThat(avtale.erGodkjentAvVeileder()).isTrue();
        assertThat(avtale.getGjeldendeInnhold().getSluttDato()).isEqualTo(nySluttDato);
    }

    @Test
    public void forleng_kun_ved_inngått_avtale() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        assertFeilkode(Feilkode.KAN_IKKE_FORLENGE_IKKE_GODKJENT_AVTALE, () -> avtale.forlengAvtale(avtale.getGjeldendeInnhold().getStartDato().plusMonths(1).minusDays(1), TestData.enNavIdent()));
    }

    @Test
    public void forleng_kun_fremover() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleGodkjentAvVeileder();
        assertFeilkode(Feilkode.KAN_IKKE_FORLENGE_FEIL_SLUTTDATO, () -> avtale.forlengAvtale(avtale.getGjeldendeInnhold().getStartDato().minusDays(1), TestData.enNavIdent()));
    }

    @Test
    public void forleng_over_4_uker_sommerjobb() {
        Now.fixedDate(LocalDate.of(2021, 6, 1));
        Avtale avtale = TestData.enSommerjobbAvtaleGodkjentAvVeileder();
        assertFeilkode(Feilkode.SOMMERJOBB_FOR_LANG_VARIGHET, () -> avtale.forlengAvtale(avtale.getGjeldendeInnhold().getStartDato().plusWeeks(4), TestData.enNavIdent()));
    }

    @Test
    public void forleng_24_mnd_midl_lts() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedSpesieltTilpassetInnsatsGodkjentAvVeileder();
        avtale.forlengAvtale(avtale.getGjeldendeInnhold().getStartDato().plusMonths(24).minusDays(1), TestData.enNavIdent());
    }

    @Test
    public void forleng_over_24_mnd_midl_lts() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedSpesieltTilpassetInnsatsGodkjentAvVeileder();
        assertFeilkode(Feilkode.VARIGHET_FOR_LANG_MIDLERTIDIG_LONNSTILSKUDD_24_MND, () -> avtale.forlengAvtale(avtale.getGjeldendeInnhold().getStartDato().plusMonths(24), TestData.enNavIdent()));
    }

    @Test
    public void forlengAvale_med_startdato_tilbake_i_tid() {
        Now.fixedDate(LocalDate.of(2021, 11, 25));
        LocalDate startDato = LocalDate.of(2021, 11, 30);
        LocalDate sluttDato = LocalDate.of(2022, 11, 25);
        Avtale avtale = TestData.enMidlertidigLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(startDato, sluttDato);
        Now.resetClock();
        LocalDate nySluttDato = avtale.getGjeldendeInnhold().getSluttDato().plusMonths(1);
        avtale.forlengAvtale(nySluttDato, TestData.enNavIdent());
        assertThat(avtale.getGjeldendeInnhold().getSluttDato()).isEqualTo(nySluttDato);
    }

    @Test
    public void forlengAvtale_forleng_etter_reduksjon_edge_case_alle_perioder_godkjent() {
        Now.fixedDate(LocalDate.of(2023, 03, 15));
        LocalDate startDato = LocalDate.of(2022, 03, 01);
        LocalDate sluttDato = LocalDate.of(2023, 02, 28);
        Avtale avtale = TestData.enMidlertidigLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(startDato, sluttDato);
        // Alle perioder er godkjent
        avtale.getTilskuddPeriode().forEach(t ->
                t.godkjenn(TestData.enNavIdent2(), "1234"));
        LocalDate nySluttDato = sluttDato.plusMonths(6);
        avtale.forlengAvtale(nySluttDato, TestData.enNavIdent());
        assertThat(avtale.getGjeldendeInnhold().getSluttDato()).isEqualTo(nySluttDato);
    }

    @Test
    public void forlengAvtale_forleng_etter_reduksjon_edge_case_alle_perioder_ubehandlet() {
        Now.fixedDate(LocalDate.of(2023, 03, 15));
        LocalDate startDato = LocalDate.of(2022, 03, 01);
        LocalDate sluttDato = LocalDate.of(2023, 02, 28);
        Avtale avtale = TestData.enMidlertidigLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(startDato, sluttDato);
        LocalDate nySluttDato = sluttDato.plusMonths(6);
        avtale.forlengAvtale(nySluttDato, TestData.enNavIdent());
        assertThat(avtale.getGjeldendeInnhold().getSluttDato()).isEqualTo(nySluttDato);
    }


    @Test
    public void sommerjobb_må_være_godkjent_av_beslutter() {
        Now.fixedDate(LocalDate.of(2021, 6, 1));
        Avtale avtale = TestData.enSommerjobbAvtaleGodkjentAvVeileder();
        assertThat(avtale.getStatus()).isEqualTo(Status.MANGLER_GODKJENNING);
        assertThat(avtale.getGjeldendeInnhold().getAvtaleInngått()).isNull();
        avtale.godkjennTilskuddsperiode(new NavIdent("B999999"), TestData.ENHET_OPPFØLGING.getVerdi());
        assertThat(avtale.getStatus()).isEqualTo(Status.GJENNOMFØRES);
        assertThat(avtale.getGjeldendeInnhold().getAvtaleInngått()).isNotNull();
    }

    @Test
    public void ikke_lonnstilskudd_skal_inngås_ved_veileders_godkjenning() {
        Avtale avtale = TestData.enArbeidstreningAvtale();

        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        Deltaker deltaker = TestData.enDeltaker(avtale);

        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        PersondataService persondataService = mock(PersondataService.class);
        EregService eregService  = mock(EregService.class);

        Veileder veileder = new Veileder(
                avtale.getVeilederNavIdent(),
                null,
                tilgangskontrollService,
                persondataService,
                mock(Norg2Client.class),
                Set.of(new NavEnhet("4802", "Trysil")),
                mock(SlettemerkeProperties.class),
                TestData.INGEN_AD_GRUPPER,
                mock(VeilarboppfolgingService.class),
                mock(FeatureToggleService.class),
                eregService
        );

        when(tilgangskontrollService.hentSkrivetilgang(veileder, avtale.getDeltakerFnr())).thenReturn(new Tilgang.Tillat());
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(veileder), any(Fnr.class))).thenReturn(true);
        when(persondataService.hentDiskresjonskode(any(Fnr.class))).thenReturn(Diskresjonskode.UGRADERT);
        when(eregService.hentVirksomhet(any())).thenReturn(new Organisasjon(TestData.etBedriftNr(), "Arbeidsplass AS"));

        veileder.endreAvtale(
                TestData.endringPåAlleArbeidstreningFelter(),
                avtale
        );
        arbeidsgiver.godkjennAvtale(avtale);
        deltaker.godkjennAvtale(avtale);

        veileder.godkjennAvtale(avtale);
        assertThat(avtale.erAvtaleInngått()).isTrue();
    }

    @Test
    public void lonnstilskudd_må_være_godkjent_av_beslutter() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        assertThat(avtale.getStatus()).isEqualTo(Status.MANGLER_GODKJENNING);
        Deltaker deltaker = TestData.enDeltaker(avtale);
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);

        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        PersondataService persondataService = mock(PersondataService.class);
        EregService eregService  = mock(EregService.class);

        Veileder veileder = new Veileder(
                avtale.getVeilederNavIdent(),
                null,
                tilgangskontrollService,
                persondataService,
                mock(Norg2Client.class),
                Set.of(new NavEnhet("4802", "Trysil")),
                mock(SlettemerkeProperties.class),
                TestData.INGEN_AD_GRUPPER,
                mock(VeilarboppfolgingService.class),
                mock(FeatureToggleService.class),
                eregService
        );

        when(tilgangskontrollService.hentSkrivetilgang(veileder, avtale.getDeltakerFnr())).thenReturn(new Tilgang.Tillat());
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(veileder), any(Fnr.class))).thenReturn(true);
        when(persondataService.hentDiskresjonskode(any(Fnr.class))).thenReturn(Diskresjonskode.UGRADERT);
        when(eregService.hentVirksomhet(any())).thenReturn(new Organisasjon(TestData.etBedriftNr(), "Arbeidsplass AS"));

        deltaker.godkjennAvtale(avtale);
        arbeidsgiver.godkjennAvtale(avtale);
        veileder.godkjennAvtale(avtale);

        assertThat(avtale.getGjeldendeInnhold().getAvtaleInngått()).isNull();
        avtale.godkjennTilskuddsperiode(new NavIdent("B999999"), TestData.ENHET_OPPFØLGING.getVerdi());
        assertThat(avtale.getGjeldendeInnhold().getAvtaleInngått()).isNotNull();
    }

    @Test
    public void lonnstilskudd_skal_generere_tilskuddsperioder() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddsjobbAvtale();
        avtale.getGjeldendeInnhold().setLonnstilskuddProsent(60);
        assertThat(avtale.getTilskuddPeriode()).isEmpty();

        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        PersondataService persondataService = mock(PersondataService.class);

        Veileder veileder = new Veileder(
            avtale.getVeilederNavIdent(),
            null,
            tilgangskontrollService,
            persondataService,
            mock(Norg2Client.class),
            Set.of(new NavEnhet("4802", "Trysil")),
            mock(SlettemerkeProperties.class),
            TestData.INGEN_AD_GRUPPER,
            mock(VeilarboppfolgingService.class),
            mock(FeatureToggleService.class),
            mock(EregService.class)
        );

        when(tilgangskontrollService.hentSkrivetilgang(veileder, avtale.getDeltakerFnr())).thenReturn(new Tilgang.Tillat());
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        veileder.endreAvtale(endreAvtale, avtale);
        assertThat(avtale.getTilskuddPeriode()).isNotEmpty();
    }

    @Test
    public void godkjenn_tilskuddsperiode_feil_enhet() {
        Now.fixedDate(LocalDate.of(2021, 6, 1));
        Avtale avtale = TestData.enSommerjobbAvtaleGodkjentAvVeileder();
        NavIdent beslutter = new NavIdent("B999999");
        assertFeilkode(Feilkode.TILSKUDDSPERIODE_ENHET_FIRE_SIFFER, () -> avtale.godkjennTilskuddsperiode(beslutter, " 4444"));
        assertFeilkode(Feilkode.TILSKUDDSPERIODE_ENHET_FIRE_SIFFER, () -> avtale.godkjennTilskuddsperiode(beslutter, "444"));
        assertFeilkode(Feilkode.TILSKUDDSPERIODE_ENHET_FIRE_SIFFER, () -> avtale.godkjennTilskuddsperiode(beslutter, "44455"));
        assertFeilkode(Feilkode.TILSKUDDSPERIODE_ENHET_FIRE_SIFFER, () -> avtale.godkjennTilskuddsperiode(beslutter, ""));
        assertFeilkode(Feilkode.TILSKUDDSPERIODE_ENHET_FIRE_SIFFER, () -> avtale.godkjennTilskuddsperiode(beslutter, null));
        avtale.godkjennTilskuddsperiode(beslutter, "4444");
    }

    @Test
    public void godkjenn_tilskuddsperiode_samme_veileder_og_beslutter() {
        Now.fixedDate(LocalDate.of(2021, 6, 1));
        Avtale avtale = TestData.enSommerjobbAvtaleGodkjentAvVeileder();

        // Kan ikke godkjenne når avtalen er tildelt seg selv
        assertFeilkode(Feilkode.TILSKUDDSPERIODE_IKKE_GODKJENNE_EGNE, () -> avtale.godkjennTilskuddsperiode(avtale.getGjeldendeInnhold().getGodkjentAvNavIdent(), "4444"));

        // Kan heller ikke godkjenne når avtalen er tildelt en annen
        avtale.overtaAvtale(new NavIdent("P887766"));
        assertFeilkode(Feilkode.TILSKUDDSPERIODE_IKKE_GODKJENNE_EGNE, () -> avtale.godkjennTilskuddsperiode(avtale.getGjeldendeInnhold().getGodkjentAvNavIdent(), "4444"));
    }

    @Test
    public void forleng_og_forkort_skal_redusere_prosent() {
        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD), Avtaleopphav.VEILEDER, TestData.enNavIdent());
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS);
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        endreAvtale.setStartDato(Now.localDate());
        endreAvtale.setSluttDato(Now.localDate().plusMonths(12).minusDays(1));
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        avtale.godkjennForDeltaker(TestData.etFodselsnummer());
        avtale.godkjennForArbeidsgiver(TestData.etFodselsnummer());
        avtale.godkjennForVeileder(TestData.enNavIdent());

        avtale.forlengAvtale(Now.localDate().plusMonths(12), TestData.enNavIdent());
        assertThat(avtale.getGjeldendeInnhold().getDatoForRedusertProsent()).isEqualTo(Now.localDate().plusMonths(12));
        assertThat(avtale.getGjeldendeInnhold().getSumLønnstilskuddRedusert()).isNotNull();

        avtale.forkortAvtale(Now.localDate().plusMonths(12).minusDays(1), ForkortetGrunn.av("grunn", ""), TestData.enNavIdent());
        assertThat(avtale.getGjeldendeInnhold().getDatoForRedusertProsent()).isNull();
        assertThat(avtale.getGjeldendeInnhold().getSumLønnstilskuddRedusert()).isNull();
    }

    @Test
    public void godkjenn_avtale_skal_ikke_gå_hvis_over_72() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        Fnr fnr = new Fnr("07075014443");
        avtale.setDeltakerFnr(fnr);
        Deltaker deltaker = TestData.enDeltaker(avtale);
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        PersondataService persondataService = mock(PersondataService.class);
        EregService eregService  = mock(EregService.class);

        Veileder veileder = new Veileder(
            avtale.getVeilederNavIdent(),
            null,
            tilgangskontrollService,
            persondataService,
            mock(Norg2Client.class),
            Set.of(new NavEnhet("4802", "Trysil")),
            mock(SlettemerkeProperties.class),
            TestData.INGEN_AD_GRUPPER,
            mock(VeilarboppfolgingService.class),
            mock(FeatureToggleService.class),
            eregService
        );

        when(tilgangskontrollService.hentSkrivetilgang(veileder, avtale.getDeltakerFnr())).thenReturn(new Tilgang.Tillat());
        when(eregService.hentVirksomhet(any())).thenReturn(new Organisasjon(TestData.etBedriftNr(), "Arbeidsplass AS"));

        deltaker.godkjennAvtale(avtale);
        arbeidsgiver.godkjennAvtale(avtale);

        assertFeilkode(Feilkode.DELTAKER_72_AAR, () -> veileder.godkjennAvtale(avtale));
    }

    // Man skal ikke kunne forkorte en avtale sånn at man får en sluttdato som er før en godkjent/utbetalt tilskuddsperide (refusjon)
    @Test
    public void kan_ikke_forkorte_forbi_utbetalt_tilskuddsperiode() {
        Now.fixedDate(LocalDate.of(2023, 1, 1));
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        Deltaker deltaker = TestData.enDeltaker(avtale);
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);

        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        PersondataService persondataService = mock(PersondataService.class);
        EregService eregService  = mock(EregService.class);

        Veileder veileder = new Veileder(
                avtale.getVeilederNavIdent(),
                null,
                tilgangskontrollService,
                persondataService,
                mock(Norg2Client.class),
                Set.of(new NavEnhet("4802", "Trysil")),
                mock(SlettemerkeProperties.class),
                TestData.INGEN_AD_GRUPPER,
                mock(VeilarboppfolgingService.class),
                mock(FeatureToggleService.class),
                eregService
        );

        when(tilgangskontrollService.hentSkrivetilgang(veileder, avtale.getDeltakerFnr())).thenReturn(new Tilgang.Tillat());
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(veileder), any(Fnr.class))).thenReturn(true);
        when(persondataService.hentDiskresjonskode(any(Fnr.class))).thenReturn(Diskresjonskode.UGRADERT);
        when(eregService.hentVirksomhet(any())).thenReturn(new Organisasjon(TestData.etBedriftNr(), "Arbeidsplass AS"));

        deltaker.godkjennAvtale(avtale);
        arbeidsgiver.godkjennAvtale(avtale);
        veileder.godkjennAvtale(avtale);

        avtale.godkjennTilskuddsperiode(new NavIdent("B999999"), TestData.ENHET_OPPFØLGING.getVerdi());
        avtale.godkjennTilskuddsperiode(new NavIdent("B999999"), TestData.ENHET_OPPFØLGING.getVerdi());
        avtale.tilskuddsperiode(0).setRefusjonStatus(RefusjonStatus.UTBETALT);
        avtale.tilskuddsperiode(1).setRefusjonStatus(RefusjonStatus.UTBETALT);

        avtale.forkortAvtale(LocalDate.of(2023, 2, 28), ForkortetGrunn.av("Grunn", "Grunn2"), veileder.getNavIdent());
        assertThat(avtale.getGjeldendeInnhold().getSluttDato()).isEqualTo(LocalDate.of(2023, 2, 28));
        assertFeilkode(Feilkode.KAN_IKKE_FORKORTE_FOR_UTBETALT_TILSKUDDSPERIODE, () -> avtale.forkortAvtale(LocalDate.of(2023, 2, 27), ForkortetGrunn.av("Grunn", "Grunn2"), veileder.getNavIdent()));

        assertThat(avtale.getGjeldendeTilskuddsperiodestatus()).isEqualTo(GODKJENT);
    }

    // Man skal ikke kunne forkorte en avtale sånn at man får en sluttdato som er før en godkjent/utbetalt tilskuddsperide (refusjon)
    @Test
    public void kan_ikke_forkorte_forbi_utbetalt_tilskuddsperiode_med_split_mitt_i_en_måned() {
        Now.fixedDate(LocalDate.of(2023, 1, 15));
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        Deltaker deltaker = TestData.enDeltaker(avtale);
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);

        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        PersondataService persondataService = mock(PersondataService.class);
        EregService eregService = mock(EregService.class);

        Veileder veileder = new Veileder(
                avtale.getVeilederNavIdent(),
                null,
                tilgangskontrollService,
                persondataService,
                mock(Norg2Client.class),
                Set.of(new NavEnhet("4802", "Trysil")),
                mock(SlettemerkeProperties.class),
                TestData.INGEN_AD_GRUPPER,
                mock(VeilarboppfolgingService.class),
                mock(FeatureToggleService.class),
                eregService
        );
        when(tilgangskontrollService.hentSkrivetilgang(veileder, avtale.getDeltakerFnr())).thenReturn(new Tilgang.Tillat());
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(veileder), any(Fnr.class))).thenReturn(true);
        when(persondataService.hentDiskresjonskode(any(Fnr.class))).thenReturn(Diskresjonskode.UGRADERT);
        when(eregService.hentVirksomhet(any())).thenReturn(new Organisasjon(TestData.etBedriftNr(), "Arbeidsplass AS"));

        deltaker.godkjennAvtale(avtale);
        arbeidsgiver.godkjennAvtale(avtale);
        veileder.godkjennAvtale(avtale);

        avtale.tilskuddsperiode(0).setRefusjonStatus(RefusjonStatus.UTBETALT);
        avtale.tilskuddsperiode(1).setRefusjonStatus(RefusjonStatus.UTBETALT);
        avtale.tilskuddsperiode(2).setRefusjonStatus(RefusjonStatus.UTBETALT);
        avtale.tilskuddsperiode(3).setRefusjonStatus(RefusjonStatus.UTBETALT);
        avtale.tilskuddsperiode(4).setRefusjonStatus(RefusjonStatus.UTBETALT);
        avtale.tilskuddsperiode(5).setRefusjonStatus(RefusjonStatus.UTBETALT);
        avtale.tilskuddsperiode(6).setRefusjonStatus(RefusjonStatus.UTBETALT);
        avtale.godkjennTilskuddsperiode(new NavIdent("B999999"), TestData.ENHET_OPPFØLGING.getVerdi());
        avtale.getTilskuddPeriode().forEach(tilskuddPeriode -> {
            System.out.print(tilskuddPeriode.getRefusjonStatus() + " ");
            System.out.println(tilskuddPeriode.getStartDato());
        });
        avtale.forkortAvtale(LocalDate.of(2023, 7, 14), ForkortetGrunn.av("Grunn", "Grunn2"), veileder.getNavIdent());
        assertThat(avtale.getGjeldendeInnhold().getSluttDato()).isEqualTo(LocalDate.of(2023, 7, 14));
        assertFeilkode(Feilkode.KAN_IKKE_FORKORTE_FOR_UTBETALT_TILSKUDDSPERIODE, () -> avtale.forkortAvtale(LocalDate.of(2023, 7, 13), ForkortetGrunn.av("Grunn", "Grunn2"), veileder.getNavIdent()));
    }

    //40%
    @Test
    public void beregning_av_lønnstilskudd_ut_ifra_kvalifiseringsgruppe_SITUASJONSBESTEMT_INNSATS_og_MIDLERTIDIG_LONNSTILSKUDD() {
        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD), Avtaleopphav.VEILEDER, TestData.enNavIdent());
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS);
        avtale.setGodkjentForEtterregistrering(true);
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        assertThat(avtale.getGjeldendeInnhold().getLonnstilskuddProsent()).isEqualTo(40);
    }

    //60%
    @Test
    public void beregning_av_lønnstilskudd_ut_ifra_kvalifiseringsgruppe_SPESIELT_TILPASSET_INNSATS_og_MIDLERTIDIG_LONNSTILSKUDD() {
        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD), Avtaleopphav.VEILEDER, TestData.enNavIdent());
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.SPESIELT_TILPASSET_INNSATS);
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        assertThat(avtale.getGjeldendeInnhold().getLonnstilskuddProsent()).isEqualTo(60);
    }

    //50%
    @Test
    @Disabled("Utleding av lønnstilskuddprosent er skrudd av på sommerjobb inntil videre for å tilltate etterregistrering")
    public void beregning_av_lønnstilskudd_ut_ifra_kvalifiseringsgruppe_SITUASJONSBESTEMT_INNSATS_og_SOMMERJOBB() {
        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.SOMMERJOBB), Avtaleopphav.VEILEDER, TestData.enNavIdent());
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS);
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        endreAvtale.setStartDato(LocalDate.of(2021, 6, 1));
        endreAvtale.setSluttDato(LocalDate.of(2021, 6, 1).plusWeeks(4).minusDays(1));
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        assertThat(avtale.getGjeldendeInnhold().getLonnstilskuddProsent()).isEqualTo(50);
    }

    //75%
    @Test
    @Disabled("Utleding av lønnstilskuddprosent er skrudd av på sommerjobb inntil videre for å tilltate etterregistrering")
    public void beregning_av_lønnstilskudd_ut_ifra_kvalifiseringsgruppe_SPESIELT_TILPASSET_INNSATS_og_SOMMERJOBB() {
        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.SOMMERJOBB), Avtaleopphav.VEILEDER, TestData.enNavIdent());
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.SPESIELT_TILPASSET_INNSATS);
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        endreAvtale.setStartDato(LocalDate.of(2021, 6, 1));
        endreAvtale.setSluttDato(LocalDate.of(2021, 6, 1).plusWeeks(4).minusDays(1));
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        assertThat(avtale.getGjeldendeInnhold().getLonnstilskuddProsent()).isEqualTo(75);
    }

    @Test
    public void godkjent_for_etterregistrering_starter_som_false() {
        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD), Avtaleopphav.VEILEDER, TestData.enNavIdent());
        assertThat(avtale.isGodkjentForEtterregistrering()).isFalse();
    }

    @Test
    public void avtale_setter_godkjent_for_etterregistrering() {
        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD), Avtaleopphav.VEILEDER, TestData.enNavIdent());
        avtale.togglegodkjennEtterregistrering(TestData.enNavIdent());
        assertThat(avtale.isGodkjentForEtterregistrering()).isTrue();
    }

    @Test
    public void avtale_kan_etterregistreres() {
        Now.fixedDate(LocalDate.of(2021, 12, 20));
        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD), Avtaleopphav.VEILEDER, TestData.enNavIdent());
        avtale.togglegodkjennEtterregistrering(TestData.enNavIdent());
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        endreAvtale.setStartDato(LocalDate.of(2021, 12, 12));
        endreAvtale.setSluttDato(LocalDate.of(2021, 12, 1).plusYears(1));
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);

        assertThat(avtale.getGjeldendeInnhold().getStartDato()).isEqualTo(LocalDate.of(2021, 12, 12));

    }

    @Test
    public void avtale_FORTIDLIG_STARTDATO() {
        Now.fixedDate(LocalDate.of(2021, 12, 20));
        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD), Avtaleopphav.VEILEDER, TestData.enNavIdent());
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        endreAvtale.setStartDato(LocalDate.of(2021, 12, 12));
        endreAvtale.setSluttDato(LocalDate.of(2021, 12, 1).plusYears(1));

        assertFeilkode(Feilkode.FORTIDLIG_STARTDATO, () -> avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER));
    }

    @Test
    public void avtale_feilkode_KAN_IKKE_MERKES_FOR_ETTERREGISTREING_AVTALE_INNGATT() {
        Avtale avtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        assertFeilkode(Feilkode.KAN_IKKE_MERKES_FOR_ETTERREGISTRERING_AVTALE_GODKJENT, () -> avtale.togglegodkjennEtterregistrering(TestData.enNavIdent()));
    }

    @Test
    public void avtale_skal_kunne_etterregistrere_med_arbeidsgiver_godkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.godkjennForArbeidsgiver(TestData.enIdentifikator());
        avtale.togglegodkjennEtterregistrering(TestData.enNavIdent());
    }

    @Test
    public void avtale_blir_ikke_inngått_når_veileder_godkjenner_om_den_må_besluttes() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        avtale.godkjennForArbeidsgiver(TestData.enIdentifikator());
        avtale.godkjennForDeltaker(TestData.enIdentifikator());
        avtale.godkjennForVeileder(TestData.enNavIdent());

        assertThat(avtale.erAvtaleInngått()).isFalse();
    }

    @Test
    public void avtale_blir_inngått_når_veileder_godkjenner_om_den_ikke_må_besluttes() {
        Avtale avtale = TestData.enArbeidstreningAvtaleMedAltUtfylt();
        avtale.godkjennForArbeidsgiver(TestData.enIdentifikator());
        avtale.godkjennForDeltaker(TestData.enIdentifikator());
        avtale.godkjennForVeileder(TestData.enNavIdent());

        assertThat(avtale.erAvtaleInngått()).isTrue();
    }

    @Test
    public void avtale_blir_inngått_ved_godkjent_tilskuddsperiode() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        avtale.godkjennForArbeidsgiver(TestData.enIdentifikator());
        avtale.godkjennForDeltaker(TestData.enIdentifikator());
        avtale.godkjennForVeileder(TestData.enNavIdent());
        avtale.godkjennTilskuddsperiode(TestData.enNavIdent2(), TestData.ENHET_OPPFØLGING.getVerdi());

        assertThat(avtale.erAvtaleInngått()).isTrue();
    }

    @Test
    public void avtale_kan_ikke_inngås_mer_enn_1_gang() {
        Avtale avtale = TestData.enArbeidstreningAvtaleMedAltUtfylt();
        avtale.godkjennForArbeidsgiver(TestData.enIdentifikator());
        avtale.godkjennForDeltaker(TestData.enIdentifikator());
        avtale.godkjennForVeileder(TestData.enNavIdent());

        assertThat(avtale.erAvtaleInngått()).isTrue();
        assertThatThrownBy(() -> avtale.godkjennForVeileder(TestData.enNavIdent())).isInstanceOf(FeilkodeException.class);
    }

    @Test
    public void avtale_kan_ikke_inngås_av_veileder_uten_beslutter_dersom_ikke_alle_tilskuddsperioder_er_behandlet_i_arena() {
        Avtale avtale = TestData.enVtaoAvtaleMedAltUtfylt();
        TilskuddPeriode tilskuddPeriode1 = mock(TilskuddPeriode.class);
        when(tilskuddPeriode1.getStatus()).thenReturn(TilskuddPeriodeStatus.BEHANDLET_I_ARENA);

        TilskuddPeriode tilskuddPeriode2 = mock(TilskuddPeriode.class);
        when(tilskuddPeriode2.getStatus()).thenReturn(TilskuddPeriodeStatus.BEHANDLET_I_ARENA);

        TilskuddPeriode tilskuddPeriode3 = mock(TilskuddPeriode.class);
        when(tilskuddPeriode3.getStatus()).thenReturn(TilskuddPeriodeStatus.UBEHANDLET);

        avtale.setTilskuddPeriode(new TreeSet<>(Set.of(
            tilskuddPeriode1,
            tilskuddPeriode2,
            tilskuddPeriode3
        )));
        avtale.setOpphav(Avtaleopphav.ARENA);
        avtale.godkjennForArbeidsgiver(TestData.enIdentifikator());
        avtale.godkjennForDeltaker(TestData.enIdentifikator());
        avtale.godkjennForVeileder(TestData.enNavIdent());

        assertThat(avtale.erAvtaleInngått()).isFalse();
    }

    @Test
    public void avtale_kan_ikke_inngås_av_veileder_uten_beslutter_dersom_alle_tilskuddsperioder_er_behandlet_i_arena_om_opphavet_til_avtalen_ikke_er_arena() {
        Avtale avtale = TestData.enVtaoAvtaleMedAltUtfylt();
        TilskuddPeriode tilskuddPeriode1 = mock(TilskuddPeriode.class);
        when(tilskuddPeriode1.getStatus()).thenReturn(TilskuddPeriodeStatus.BEHANDLET_I_ARENA);

        TilskuddPeriode tilskuddPeriode2 = mock(TilskuddPeriode.class);
        when(tilskuddPeriode2.getStatus()).thenReturn(TilskuddPeriodeStatus.BEHANDLET_I_ARENA);

        TilskuddPeriode tilskuddPeriode3 = mock(TilskuddPeriode.class);
        when(tilskuddPeriode3.getStatus()).thenReturn(TilskuddPeriodeStatus.BEHANDLET_I_ARENA);

        avtale.setTilskuddPeriode(new TreeSet<>(Set.of(
            tilskuddPeriode1,
            tilskuddPeriode2,
            tilskuddPeriode3
        )));
        avtale.setOpphav(Avtaleopphav.VEILEDER);
        avtale.godkjennForArbeidsgiver(TestData.enIdentifikator());
        avtale.godkjennForDeltaker(TestData.enIdentifikator());
        avtale.godkjennForVeileder(TestData.enNavIdent());

        assertThat(avtale.erAvtaleInngått()).isFalse();
    }

    @Test
    public void avtale_kan_inngås_av_veileder_uten_beslutter_dersom_alle_tilskuddsperioder_er_behandlet_i_arena() {
        Avtale avtale = TestData.enVtaoAvtaleMedAltUtfylt();
        TilskuddPeriode tilskuddPeriode1 = mock(TilskuddPeriode.class);
        when(tilskuddPeriode1.getStatus()).thenReturn(TilskuddPeriodeStatus.BEHANDLET_I_ARENA);

        TilskuddPeriode tilskuddPeriode2 = mock(TilskuddPeriode.class);
        when(tilskuddPeriode2.getStatus()).thenReturn(TilskuddPeriodeStatus.BEHANDLET_I_ARENA);

        TilskuddPeriode tilskuddPeriode3 = mock(TilskuddPeriode.class);
        when(tilskuddPeriode3.getStatus()).thenReturn(TilskuddPeriodeStatus.BEHANDLET_I_ARENA);

        avtale.setTilskuddPeriode(new TreeSet<>(Set.of(
            tilskuddPeriode1,
            tilskuddPeriode2,
            tilskuddPeriode3
        )));
        avtale.setOpphav(Avtaleopphav.ARENA);
        avtale.godkjennForArbeidsgiver(TestData.enIdentifikator());
        avtale.godkjennForDeltaker(TestData.enIdentifikator());
        avtale.godkjennForVeileder(TestData.enNavIdent());

        assertThat(avtale.erAvtaleInngått()).isTrue();
    }

}
