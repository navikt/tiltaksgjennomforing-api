package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.*;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AvtaleTest {

    @Test
    public void nyAvtaleFactorySkalReturnereRiktigeStandardverdier() {
        Fnr deltakerFnr = new Fnr("23078637692");

        NavIdent veilederNavIdent = new NavIdent("X123456");
        BedriftNr bedriftNr = new BedriftNr("000111222");
        Avtale avtale = Avtale.veilederOppretterAvtale(new OpprettAvtale(deltakerFnr, bedriftNr, Tiltakstype.ARBEIDSTRENING), veilederNavIdent);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(avtale.getOpprettetTidspunkt()).isNotNull();
            softly.assertThat(avtale.getDeltakerFnr()).isEqualTo(deltakerFnr);
            softly.assertThat(avtale.getDeltakerTlf()).isNull();
            softly.assertThat(avtale.getMaal()).isEmpty();
            softly.assertThat(avtale.getDeltakerFornavn()).isNull();
            softly.assertThat(avtale.getDeltakerEtternavn()).isNull();
            softly.assertThat(avtale.getBedriftNavn()).isNull();
            softly.assertThat(avtale.getBedriftNr()).isEqualTo(bedriftNr);
            softly.assertThat(avtale.getArbeidsgiverFornavn()).isNull();
            softly.assertThat(avtale.getArbeidsgiverEtternavn()).isNull();
            softly.assertThat(avtale.getArbeidsgiverTlf()).isNull();
            softly.assertThat(avtale.getVeilederFornavn()).isNull();
            softly.assertThat(avtale.getVeilederEtternavn()).isNull();
            softly.assertThat(avtale.getVeilederTlf()).isNull();
            softly.assertThat(avtale.getOppfolging()).isNull();
            softly.assertThat(avtale.getTilrettelegging()).isNull();
            softly.assertThat(avtale.getStartDato()).isNull();
            softly.assertThat(avtale.getSluttDato()).isNull();
            softly.assertThat(avtale.getStillingprosent()).isNull();
            softly.assertThat(avtale.getStillingstittel()).isNull();
            softly.assertThat(avtale.erGodkjentAvDeltaker()).isFalse();
            softly.assertThat(avtale.erGodkjentAvArbeidsgiver()).isFalse();
            softly.assertThat(avtale.erGodkjentAvVeileder()).isFalse();
        });
    }

    @Test
    public void nyAvtaleSkalFeileHvisManglerDeltaker() {
        assertThatThrownBy(() -> Avtale.veilederOppretterAvtale(new OpprettAvtale(null, new BedriftNr("111222333"), Tiltakstype.ARBEIDSTRENING), new NavIdent("X123456"))).isInstanceOf(TiltaksgjennomforingException.class);
    }

    @Test
    public void nyAvtaleSkalFeileHvisManglerArbeidsgiver() {
        assertThatThrownBy(() -> Avtale.veilederOppretterAvtale(new OpprettAvtale(new Fnr("23078637692"), null, Tiltakstype.ARBEIDSTRENING), new NavIdent("X123456"))).isInstanceOf(TiltaksgjennomforingException.class);
    }

    @Test
    public void nyAvtaleSkalFeileHvisManglerVeileder() {
        assertThatThrownBy(() -> Avtale.veilederOppretterAvtale(new OpprettAvtale(new Fnr("23078637692"), new BedriftNr("000111222"), Tiltakstype.ARBEIDSTRENING), null)).isInstanceOf(TiltaksgjennomforingException.class);
    }

    @Test
    public void nyAvtaleSkalFeileHvisDeltakerErForUng() {
        assertFeilkode(Feilkode.IKKE_GAMMEL_NOK, () -> Avtale.veilederOppretterAvtale(new OpprettAvtale(new Fnr("24010970772"), new BedriftNr("000111222"), Tiltakstype.ARBEIDSTRENING), null));
    }
    @Test
    public void nyAvtaleSkalFeileHvisDeltakerErForGammelForSommerjobb() {
        assertFeilkode(Feilkode.FOR_GAMMEL, () -> Avtale.veilederOppretterAvtale(new OpprettAvtale(new Fnr("08098114468"), new BedriftNr("000111222"), Tiltakstype.SOMMERJOBB), null));
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
        avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(avtale.getDeltakerFornavn()).isEqualTo(endreAvtale.getDeltakerFornavn());
            softly.assertThat(avtale.getDeltakerEtternavn()).isEqualTo(endreAvtale.getDeltakerEtternavn());
            softly.assertThat(avtale.getDeltakerTlf()).isEqualTo(endreAvtale.getDeltakerTlf());
            softly.assertThat(avtale.getBedriftNavn()).isEqualTo(endreAvtale.getBedriftNavn());
            softly.assertThat(avtale.getArbeidsgiverFornavn()).isEqualTo(endreAvtale.getArbeidsgiverFornavn());
            softly.assertThat(avtale.getArbeidsgiverEtternavn()).isEqualTo(endreAvtale.getArbeidsgiverEtternavn());
            softly.assertThat(avtale.getArbeidsgiverTlf()).isEqualTo(endreAvtale.getArbeidsgiverTlf());
            softly.assertThat(avtale.getVeilederFornavn()).isEqualTo(endreAvtale.getVeilederFornavn());
            softly.assertThat(avtale.getVeilederEtternavn()).isEqualTo(endreAvtale.getVeilederEtternavn());
            softly.assertThat(avtale.getVeilederTlf()).isEqualTo(endreAvtale.getVeilederTlf());
            softly.assertThat(avtale.getOppfolging()).isEqualTo(endreAvtale.getOppfolging());
            softly.assertThat(avtale.getTilrettelegging()).isEqualTo(endreAvtale.getTilrettelegging());
            softly.assertThat(avtale.getStartDato()).isEqualTo(endreAvtale.getStartDato());
            softly.assertThat(avtale.getSluttDato()).isEqualTo(endreAvtale.getSluttDato());
            softly.assertThat(avtale.getStillingprosent()).isEqualTo(endreAvtale.getStillingprosent());
            softly.assertThat(avtale.getMaal()).isEqualTo(endreAvtale.getMaal());
            softly.assertThat(avtale.getStillingstittel()).isEqualTo(endreAvtale.getStillingstittel());
        });
    }

    @Test
    public void endreAvtale__for_langt_maal_skal_feile() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        Maal etMaal = TestData.etMaal();
        etMaal.setBeskrivelse("Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.Dette er en string pa 1024 tegn.");
        EndreAvtale endreAvtale = new EndreAvtale();
        endreAvtale.setMaal(List.of(etMaal));
        assertThatThrownBy(() -> avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()))).isInstanceOf(TiltaksgjennomforingException.class);
    }

    @Test
    public void endreAvtale__startdato_satt_men_ikke_sluttdato() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        EndreAvtale endreAvtale = new EndreAvtale();
        LocalDate startDato = Now.localDate();
        endreAvtale.setStartDato(startDato);
        avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()));
        assertThat(avtale.getStartDato()).isEqualTo(startDato);
    }

    @Test
    public void endreAvtale__sluttdato_satt_men_ikke_startdato() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        EndreAvtale endreAvtale = new EndreAvtale();
        LocalDate sluttDato = Now.localDate();
        endreAvtale.setSluttDato(sluttDato);
        avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()));
        assertThat(avtale.getSluttDato()).isEqualTo(sluttDato);
    }

    @Test
    public void endreAvtale__startdato_og_sluttdato_satt_18mnd() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        EndreAvtale endreAvtale = new EndreAvtale();
        LocalDate startDato = Now.localDate();
        LocalDate sluttDato = startDato.plusMonths(18);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()));
        assertThat(avtale.getStartDato()).isEqualTo(startDato);
        assertThat(avtale.getSluttDato()).isEqualTo(sluttDato);
    }

    @Test
    public void endreAvtale__startdato_og_sluttdato_satt_over_18mnd() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        EndreAvtale endreAvtale = new EndreAvtale();
        LocalDate startDato = Now.localDate();
        LocalDate sluttDato = startDato.plusMonths(18).plusDays(1);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        assertThatThrownBy(() -> avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()))).isInstanceOf(VarighetForLangArbeidstreningException.class);
    }

    @Test
    public void endreAvtale__startdato_er_etter_sluttdato() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        EndreAvtale endreAvtale = new EndreAvtale();
        LocalDate startDato = Now.localDate();
        LocalDate sluttDato = startDato.minusDays(1);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        assertFeilkode(Feilkode.START_ETTER_SLUTT, () -> avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype())));
    }

    @Test
    public void endreAvtaleSkalIkkeEndreGodkjenninger() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        boolean deltakerGodkjenningFoerEndring = avtale.erGodkjentAvDeltaker();
        boolean arbeidsgiverGodkjenningFoerEndring = avtale.erGodkjentAvArbeidsgiver();
        boolean veilederGodkjenningFoerEndring = avtale.erGodkjentAvVeileder();

        avtale.endreAvtale(Now.instant(), TestData.endringPåAlleArbeidstreningFelter(), Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()));

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
        avtale.endreAvtale(førstEndret, TestData.ingenEndring(), Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()));
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

        Avtale avtale = Avtale.veilederOppretterAvtale(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.ARBEIDSTRENING), TestData.enNavIdent());

        testAtAlleFelterMangler(avtale, arbeidstreningsfelter);
        testAtHvertEnkeltFeltMangler(avtale, arbeidstreningsfelter);
    }

    @Test
    public void kanIkkeGodkjennesNårNoeMangler__lønnstilskudd() {
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
                AvtaleInnhold.Fields.lonnstilskuddProsent,
                AvtaleInnhold.Fields.arbeidsgiverKontonummer,
                AvtaleInnhold.Fields.manedslonn,
                AvtaleInnhold.Fields.feriepengesats,
                AvtaleInnhold.Fields.otpSats,
                AvtaleInnhold.Fields.arbeidsgiveravgift,
                AvtaleInnhold.Fields.tilrettelegging,
                AvtaleInnhold.Fields.oppfolging,
                AvtaleInnhold.Fields.harFamilietilknytning
        );

        Avtale avtale = Avtale.veilederOppretterAvtale(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD), TestData.enNavIdent());

        testAtAlleFelterMangler(avtale, lønnstilskuddfelter);
        testAtHvertEnkeltFeltMangler(avtale, lønnstilskuddfelter);
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
                AvtaleInnhold.Fields.antallDagerPerUke,
                AvtaleInnhold.Fields.mentorFornavn,
                AvtaleInnhold.Fields.mentorEtternavn,
                AvtaleInnhold.Fields.mentorTimelonn,
                AvtaleInnhold.Fields.mentorAntallTimer,
                AvtaleInnhold.Fields.mentorOppgaver,
                AvtaleInnhold.Fields.tilrettelegging,
                AvtaleInnhold.Fields.oppfolging
        );

        Avtale avtale = Avtale.veilederOppretterAvtale(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MENTOR), TestData.enNavIdent());

        testAtAlleFelterMangler(avtale, mentorfelter);
        testAtHvertEnkeltFeltMangler(avtale, mentorfelter);
    }

    private static void testAtHvertEnkeltFeltMangler(Avtale avtale, Set<String> felterSomKrevesForTiltakstype) {
        for (String felt : felterSomKrevesForTiltakstype) {
            EndreAvtale endreAvtale = endringPåAltUtenom(felt);
            avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()));
            assertThat(avtale.felterSomIkkeErFyltUt()).containsOnly(felt);
            assertFeilkode(Feilkode.ALT_MA_VAERE_FYLT_UT, () -> avtale.godkjennForArbeidsgiver(TestData.enIdentifikator()));
        }
    }

    private static void testAtAlleFelterMangler(Avtale avtale, Set<String> arbeidstreningsfelter) {
        assertThat(avtale.felterSomIkkeErFyltUt()).containsExactlyInAnyOrderElementsOf(arbeidstreningsfelter);
    }

    private static EndreAvtale endringPåAltUtenom(String felt) {
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
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
        avtale.sjekkOmAltErUtfylt();
    }

    @Test
    public void status__ny_avtale() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        assertThat(avtale.status()).isEqualTo(Status.PÅBEGYNT.getBeskrivelse());
    }

    @Test
    public void status__null_startdato() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        avtale.setStartDato(null);
        avtale.setSluttDato(null);
        assertThat(avtale.status()).isEqualTo(Status.PÅBEGYNT.getBeskrivelse());
    }

    @Test
    public void status__noe_fylt_ut() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        avtale.setStartDato(Now.localDate().plusDays(5));
        avtale.setSluttDato(avtale.getStartDato().plusMonths(3));
        avtale.setBedriftNavn("testbedriftsnavn");
        assertThat(avtale.status()).isEqualTo(Status.PÅBEGYNT.getBeskrivelse());
    }

    @Test
    public void status__avsluttet_i_gaar() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setStartDato(Now.localDate().minusWeeks(4).minusDays(1));
        avtale.setSluttDato(avtale.getStartDato().plusWeeks(4));
        avtale.setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.setGodkjentAvDeltaker(Now.localDateTime());
        avtale.setGodkjentAvVeileder(Now.localDateTime());
        avtale.setAvtaleInngått(Now.localDateTime());
        assertThat(avtale.status()).isEqualTo(Status.AVSLUTTET.getBeskrivelse());
    }

    @Test
    public void status__avslutter_i_dag() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setStartDato(Now.localDate().minusWeeks(4));
        avtale.setSluttDato(avtale.getStartDato().plusWeeks(4));
        avtale.setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.setGodkjentAvDeltaker(Now.localDateTime());
        avtale.setGodkjentAvVeileder(Now.localDateTime());
        avtale.setAvtaleInngått(Now.localDateTime());
        assertThat(avtale.status()).isEqualTo(Status.GJENNOMFØRES.getBeskrivelse());
    }

    @Test
    public void status__startet_i_dag() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setStartDato(Now.localDate());
        avtale.setSluttDato(avtale.getStartDato().plusWeeks(4));
        avtale.setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.setGodkjentAvDeltaker(Now.localDateTime());
        avtale.setGodkjentAvVeileder(Now.localDateTime());
        avtale.setAvtaleInngått(Now.localDateTime());
        assertThat(avtale.status()).isEqualTo(Status.GJENNOMFØRES.getBeskrivelse());
    }

    @Test
    public void status__starter_i_morgen() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setStartDato(Now.localDate().plusDays(1));
        avtale.setSluttDato(avtale.getStartDato().plusWeeks(4));
        avtale.setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.setGodkjentAvDeltaker(Now.localDateTime());
        avtale.setGodkjentAvVeileder(Now.localDateTime());
        avtale.setAvtaleInngått(Now.localDateTime());
        assertThat(avtale.status()).isEqualTo(Status.KLAR_FOR_OPPSTART.getBeskrivelse());
    }

    @Test
    public void status__klar_for_godkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        assertThat(avtale.status()).isEqualTo(Status.MANGLER_GODKJENNING.getBeskrivelse());
    }

    @Test
    public void status__veileder_har_godkjent() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setStartDato(Now.localDate().plusDays(1));
        avtale.setSluttDato(Now.localDate().plusDays(1).plusMonths(1));
        avtale.setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.setGodkjentAvDeltaker(Now.localDateTime());
        avtale.setGodkjentAvVeileder(Now.localDateTime());
        avtale.setAvtaleInngått(Now.localDateTime());
        assertThat(avtale.status()).isEqualTo(Status.KLAR_FOR_OPPSTART.getBeskrivelse());
    }

    @Test
    public void status__naar_deltaker_tlf_mangler() {
        // Deltaker tlf ble innført etter at avtaler er opprettet. Det kan derfor være
        // avtaler som er inngått som mangler tlf.
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setStartDato(Now.localDate().minusDays(1));
        avtale.setSluttDato(Now.localDate().minusDays(1).plusMonths(1));
        avtale.setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.setGodkjentAvDeltaker(Now.localDateTime());
        avtale.setGodkjentAvVeileder(Now.localDateTime());
        avtale.setAvtaleInngått(Now.localDateTime());
        avtale.setDeltakerTlf(null);
        assertThat(avtale.status()).isEqualTo(Status.GJENNOMFØRES.getBeskrivelse());
    }

    @Test
    public void status__avbrutt() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        avtale.avbryt(TestData.enVeileder(avtale), new AvbruttInfo(Now.localDate(), "grunnen"));
        assertThat(avtale.status()).isEqualTo(Status.AVBRUTT.getBeskrivelse());
        assertThat(avtale.getAvbruttDato()).isNotNull();
        assertThat(avtale.getAvbruttGrunn()).isEqualTo("grunnen");
    }

    @Test
    public void avbryt_ufordelt_avtale_skal_bli_fordelt() {
        Avtale avtale = TestData.enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordelt();
        Veileder veileder = TestData.enVeileder(new NavIdent("Z123456"));
        avtale.avbryt(veileder, new AvbruttInfo(Now.localDate(), "grunnen"));

        assertThat(avtale.status()).isEqualTo(Status.AVBRUTT.getBeskrivelse());
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
    public void status__gjenopprettet() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        avtale.setAvbrutt(true);
        avtale.setAvbruttDato(Now.localDate());
        avtale.setAvbruttGrunn("enGrunn");

        avtale.gjenopprett(TestData.enVeileder(avtale));
        assertThat(avtale.status()).isNotEqualTo(Status.AVBRUTT.getBeskrivelse());
        assertThat(avtale.getAvbruttDato()).isNull();
        assertThat(avtale.getAvbruttGrunn()).isNull();
    }

    @Test
    public void ikke_avbrutt_avtale_kan_ikke_gjenopprettes() {
        Avtale godkjentAvtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        assertThat(godkjentAvtale.kanGjenopprettes()).isFalse();
    }

    @Test
    public void avbrutt_avtale_kan_gjenopprettes() {
        Avtale godkjentAvtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        godkjentAvtale.setAvbrutt(true);
        assertThat(godkjentAvtale.kanGjenopprettes()).isTrue();
    }

    @Test
    public void kanLåsesOpp__skal_være_true_når_godkjent_av_veileder() {
        assertThat(TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder().kanLåsesOpp()).isTrue();
    }

    @Test
    public void kanLåsesOpp__skal_være_false_når_ikke_godkjent_av_veileder() {
        Avtale avtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        avtale.setGodkjentAvVeileder(null);
        assertThat(avtale.kanLåsesOpp()).isFalse();
    }

    @Test
    public void låsOppAvtale__skal_ha_riktig_innhold() {
        Avtale avtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        Avtale avtaleKopi = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();

        avtale.låsOppAvtale();
        assertThat(avtale).isEqualToIgnoringGivenFields(avtaleKopi,
                Avtale.Fields.id,
                Avtale.Fields.opprettetTidspunkt,
                Avtale.Fields.sistEndret,
                Avtale.Fields.versjoner,
                AvtaleInnhold.Fields.godkjentAvDeltaker,
                AvtaleInnhold.Fields.godkjentAvArbeidsgiver,
                AvtaleInnhold.Fields.godkjentAvVeileder,
                "domainEvents");
        assertThat(avtale.getGodkjentAvDeltaker()).isNull();
        assertThat(avtale.getGodkjentAvArbeidsgiver()).isNull();
        assertThat(avtale.getGodkjentAvVeileder()).isNull();
        assertThat(avtale.getJournalpostId()).isNull();
    }

    @Test
    public void låsOppAvtale__skal_lage_ny_versjon() {
        Avtale avtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        avtale.låsOppAvtale();
        List<AvtaleInnhold> versjoner = avtale.getVersjoner();

        AvtaleInnhold førsteVersjon = versjoner.get(0);
        AvtaleInnhold andreVersjon = versjoner.get(1);
        assertThat(førsteVersjon.getVersjon()).isEqualTo(1);
        assertThat(førsteVersjon.getInnholdType()).isEqualTo(AvtaleInnholdType.INNGÅ);
        assertThat(førsteVersjon.getVersjon()).isEqualTo(1);
        assertThat(andreVersjon.getVersjon()).isEqualTo(2);
        assertThat(andreVersjon.getMaal())
                .usingElementComparatorOnFields(Maal.Fields.kategori, Maal.Fields.beskrivelse)
                .isEqualTo(førsteVersjon.getMaal());
        assertThat(andreVersjon.getInnholdType()).isEqualTo(AvtaleInnholdType.LÅSE_OPP);
        assertThat(andreVersjon).isEqualToIgnoringGivenFields(førsteVersjon,
                AvtaleInnhold.Fields.id,
                AvtaleInnhold.Fields.versjon,
                AvtaleInnhold.Fields.maal,
                AvtaleInnhold.Fields.godkjentAvDeltaker,
                AvtaleInnhold.Fields.godkjentAvArbeidsgiver,
                AvtaleInnhold.Fields.godkjentAvVeileder,
                AvtaleInnhold.Fields.avtaleInngått,
                AvtaleInnhold.Fields.ikrafttredelsestidspunkt,
                AvtaleInnhold.Fields.journalpostId,
                AvtaleInnhold.Fields.innholdType);
    }

    @Test
    public void sistEndretNå__kalles_ved_endreAvtale() throws InterruptedException {
        Instant førEndringen = Now.instant();
        Avtale avtale = TestData.enArbeidstreningAvtale();
        Thread.sleep(10);
        avtale.endreAvtale(Instant.MAX, TestData.ingenEndring(), Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()));
        assertThat(avtale.getSistEndret()).isAfter(førEndringen);
    }

    @Test
    public void sistEndretNå__kalles_ved_godkjennForDeltaker() throws InterruptedException {
        Instant førEndringen = Now.instant();
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Thread.sleep(10);
        avtale.godkjennForDeltaker(TestData.enIdentifikator());
        assertThat(avtale.getSistEndret()).isAfter(førEndringen);
    }

    @Test
    public void sistEndretNå__kalles_ved_godkjennForArbeidsgiver() throws InterruptedException {
        Instant førEndringen = Now.instant();
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Thread.sleep(10);
        avtale.godkjennForArbeidsgiver(TestData.enIdentifikator());
        assertThat(avtale.getSistEndret()).isAfter(førEndringen);
    }

    @Test
    public void sistEndretNå__kalles_ved_godkjennForVeileder() throws InterruptedException {
        Instant førEndringen = Now.instant();
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Thread.sleep(10);
        avtale.godkjennForArbeidsgiver(TestData.enIdentifikator());
        assertThat(avtale.getSistEndret()).isAfter(førEndringen);
    }

    @Test
    public void sistEndretNå__kalles_ved_godkjennForVeilederOgDeltaker() throws InterruptedException {
        Instant førEndringen = Now.instant();
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvArbeidsgiver(Now.localDateTime());
        Thread.sleep(10);
        avtale.godkjennForVeilederOgDeltaker(TestData.enNavIdent(), TestData.enGodkjentPaVegneGrunn());
        assertThat(avtale.getSistEndret()).isAfter(førEndringen);
    }

    @Test
    public void sistEndretNå__kalles_ved_opphevGodkjenningerSomArbeidsgiver() throws InterruptedException {
        Instant førEndringen = Now.instant();
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Thread.sleep(10);
        avtale.opphevGodkjenningerSomArbeidsgiver();
        assertThat(avtale.getSistEndret()).isAfter(førEndringen);
    }

    @Test
    public void sistEndretNå__kalles_ved_opphevGodkjenningerSomVeileder() throws InterruptedException {
        Instant førEndringen = Now.instant();
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Thread.sleep(10);
        avtale.opphevGodkjenningerSomVeileder();
        assertThat(avtale.getSistEndret()).isAfter(førEndringen);
    }

    @Test
    public void sistEndretNå__kalles_ved_avbryt() throws InterruptedException {
        Instant førEndringen = Now.instant();
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Thread.sleep(10);
        avtale.avbryt(TestData.enVeileder(avtale), new AvbruttInfo());
        assertThat(avtale.getSistEndret()).isAfter(førEndringen);
    }

    @Test
    public void sistEndretNå__kalles_ved_låsOppAvtale() throws InterruptedException {
        Instant førEndringen = Now.instant();
        Avtale avtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        Thread.sleep(10);
        avtale.låsOppAvtale();
        assertThat(avtale.getSistEndret()).isAfter(førEndringen);
    }

    @Test
    public void avtaleklarForOppstart() {
        Avtale avtale = TestData.enAvtaleKlarForOppstart();
        assertThat(avtale.status()).isEqualTo(Status.KLAR_FOR_OPPSTART.getBeskrivelse());
    }

    @Test
    public void avtale_opprettet_av_arbedsgiver_skal_være_ufordelt() {
        Avtale avtale = Avtale.arbeidsgiverOppretterAvtale(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD));
        assertThat(avtale.isOpprettetAvArbeidsgiver()).isTrue();
        assertThat(avtale.erUfordelt()).isTrue();
    }

    @Test
    public void avtale_kan_være_ufordelt_selv_om_alt_er_utfylt() {
        Avtale avtale = Avtale.arbeidsgiverOppretterAvtale(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD));
        avtale.endreAvtale(Now.instant(), TestData.endringPåAlleFelter(), Avtalerolle.ARBEIDSGIVER, EnumSet.of(avtale.getTiltakstype()));
        assertThat(avtale.erUfordelt()).isTrue();
    }

    @Test
    public void avtale_skal_kunne_godkjennes_når_den_erUfordelt() {
        Avtale avtale = Avtale.arbeidsgiverOppretterAvtale(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD));
        avtale.endreAvtale(Now.instant(), TestData.endringPåAlleFelter(), Avtalerolle.ARBEIDSGIVER, EnumSet.of(avtale.getTiltakstype()));
        avtale.godkjennForArbeidsgiver(TestData.enIdentifikator());
        avtale.godkjennForDeltaker(TestData.enIdentifikator());
        assertThat(avtale.erGodkjentAvArbeidsgiver()).isTrue();
        assertThat(avtale.erGodkjentAvDeltaker()).isTrue();
    }

    @Test
    public void ufordelt_avtale_må_tildeles_før_veileder_godkjenner() {
        Avtale avtale = Avtale.arbeidsgiverOppretterAvtale(
            new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD));
        avtale.endreAvtale(Now.instant(), TestData.endringPåAlleFelter(), Avtalerolle.ARBEIDSGIVER, EnumSet.of(avtale.getTiltakstype()));
        avtale.godkjennForArbeidsgiver(TestData.enIdentifikator());
        avtale.godkjennForDeltaker(TestData.enIdentifikator());
        assertThatThrownBy(() -> avtale.godkjennForVeileder(TestData.enNavIdent())).isInstanceOf(AvtaleErIkkeFordeltException.class);
    }


    @Test
    public void ufordelt_midlertidig_lts_avtale_endrer_avtale_med_lavere_lønnstilskuddprosent_enn_mellom_kun_40_60_prosent() {
        Avtale avtale = Avtale
            .veilederOppretterAvtale(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD),
                new NavIdent("Z123456"));
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setLonnstilskuddProsent(20);
        assertThatThrownBy(() -> avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype())))
            .isInstanceOf(FeilLonnstilskuddsprosentException.class);
    }

    @Test
    public void ufordelt_midlertidig_lts_avtale_endrer_avtale_med_høyere_enn_maks_lønnstilskuddprosent_enn_60_prosent() {
        Avtale avtale = Avtale
            .veilederOppretterAvtale(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD),
                new NavIdent("Z123456"));
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setLonnstilskuddProsent(67);
        assertThatThrownBy(() -> avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype())))
            .isInstanceOf(FeilLonnstilskuddsprosentException.class);
    }


    @Test
    public void ufordelt_varig_lts_avtale_endrer_avtale_med_lavere_lønnstilskuddprosent_enn_0_prosent() {
        Avtale avtale = Avtale
            .veilederOppretterAvtale(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.VARIG_LONNSTILSKUDD),
                new NavIdent("Z123456"));
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setLonnstilskuddProsent(-1);
        assertThatThrownBy(() -> avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype())))
            .isInstanceOf(FeilLonnstilskuddsprosentException.class);
    }

    @Test
    public void ufordelt_varig_lts_avtale_endrer_avtale_med_høyere_enn_maks_lønnstilskuddprosent_enn_75_prosent() {
        Avtale avtale = Avtale
            .veilederOppretterAvtale(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.VARIG_LONNSTILSKUDD),
                new NavIdent("Z123456"));
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setLonnstilskuddProsent(100);
        assertThatThrownBy(() -> avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype())))
            .isInstanceOf(FeilLonnstilskuddsprosentException.class);
    }

    @Test
    public void endre_tilskuddsberegning_setter_riktige_felter() {
        Avtale avtale = TestData.enLonnstilskuddAvtaleGodkjentAvVeileder();
        double otpSats = 0.048;
        BigDecimal feriepengesats = new BigDecimal("0.166");
        BigDecimal arbeidsgiveravgift = BigDecimal.ZERO;
        int manedslonn = 44444;

        avtale.endreTilskuddsberegning(EndreTilskuddsberegning.builder().otpSats(otpSats).feriepengesats(feriepengesats).arbeidsgiveravgift(arbeidsgiveravgift).manedslonn(manedslonn).build(), TestData.enNavIdent());

        assertThat(avtale.getVersjoner()).hasSize(2);
        assertThat(avtale.erGodkjentAvVeileder()).isTrue();
        assertThat(avtale.getSumLonnstilskudd()).isPositive();
        assertThat(avtale.getOtpSats()).isEqualTo(otpSats);
        assertThat(avtale.getFeriepengesats()).isEqualTo(feriepengesats);
        assertThat(avtale.getArbeidsgiveravgift()).isEqualTo(arbeidsgiveravgift);
        assertThat(avtale.getManedslonn()).isEqualTo(manedslonn);
    }

    @Test
    public void endre_stillingsbeskrivelse_setter_riktige_felter() {
        Avtale avtale = TestData.enLonnstilskuddAvtaleGodkjentAvVeileder();
        String stillingstittel = "Kokk";
        String arbeidsoppgaver = "Lage mat";
        Integer stillingStyrk08 = 1234;
        Integer stillingKonseptId = 9999;
        Integer stillingprosent = 90;
        Integer antallDagerPerUke = 4;
        var endreStillingsbeskrivelse = new EndreStillingsbeskrivelse(stillingstittel, arbeidsoppgaver, stillingStyrk08, stillingKonseptId, stillingprosent, antallDagerPerUke).toBuilder().build();
        avtale.endreStillingsbeskrivelse(endreStillingsbeskrivelse, new NavIdent("Z123456"));

        assertThat(avtale.getStillingstittel()).isEqualTo(stillingstittel);
        assertThat(avtale.getArbeidsoppgaver()).isEqualTo(arbeidsoppgaver);
        assertThat(avtale.getStillingStyrk08()).isEqualTo(stillingStyrk08);
        assertThat(avtale.getStillingKonseptId()).isEqualTo(stillingKonseptId);
        assertThat(avtale.getStillingprosent()).isEqualTo(stillingprosent);
        assertThat(avtale.getAntallDagerPerUke()).isEqualTo(antallDagerPerUke);
    }

    @Test
    public void endre_tilskuddsberegning_kun_inngått_avtale() {
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
        assertFeilkode(Feilkode.KAN_IKKE_ENDRE_OKONOMI_IKKE_GODKJENT_AVTALE, () -> avtale.endreTilskuddsberegning(TestData.enEndreTilskuddsberegning(), TestData.enNavIdent()));
    }

    @Test
    public void endre_tilskuddsberegning_ugyldig_input() {
        Avtale avtale = TestData.enLonnstilskuddAvtaleGodkjentAvVeileder();
        assertFeilkode(Feilkode.KAN_IKKE_ENDRE_OKONOMI_UGYLDIG_INPUT, () -> avtale.endreTilskuddsberegning(TestData.enEndreTilskuddsberegning().toBuilder().manedslonn(null).build(), TestData.enNavIdent()));
    }

    @Test
    public void forleng_setter_riktige_felter() {
        Avtale avtale = TestData.enArbeidstreningAvtaleGodkjentAvVeileder();
        LocalDate nySluttDato = avtale.getSluttDato().plusMonths(1);

        avtale.forlengAvtale(nySluttDato, TestData.enNavIdent());

        assertThat(avtale.getVersjoner()).hasSize(2);
        assertThat(avtale.erGodkjentAvVeileder()).isTrue();
        assertThat(avtale.getSluttDato()).isEqualTo(nySluttDato);
    }

    @Test
    public void forleng_kun_ved_inngått_avtale() {
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
        assertFeilkode(Feilkode.KAN_IKKE_FORLENGE_IKKE_GODKJENT_AVTALE, () -> avtale.forlengAvtale(avtale.getSluttDato().plusMonths(1), TestData.enNavIdent()));
    }

    @Test
    public void forleng_kun_fremover() {
        Avtale avtale = TestData.enLonnstilskuddAvtaleGodkjentAvVeileder();
        assertFeilkode(Feilkode.KAN_IKKE_FORLENGE_FEIL_SLUTTDATO, () -> avtale.forlengAvtale(avtale.getSluttDato().minusDays(1), TestData.enNavIdent()));
    }

    @Test
    public void forleng_over_4_uker_sommerjobb() {
        Avtale avtale = TestData.enSommerjobbAvtaleGodkjentAvVeileder();
        assertFeilkode(Feilkode.SOMMERJOBB_FOR_LANG_VARIGHET, () -> avtale.forlengAvtale(avtale.getStartDato().plusWeeks(4).plusDays(1), TestData.enNavIdent()));
    }

    @Test
    public void forleng_24_mnd_midl_lts() {
        Avtale avtale = TestData.enLonnstilskuddAvtaleGodkjentAvVeileder();
        avtale.forlengAvtale(avtale.getStartDato().plusMonths(24), TestData.enNavIdent());
    }

    @Test
    public void forleng_over_24_mnd_midl_lts() {
        Avtale avtale = TestData.enLonnstilskuddAvtaleGodkjentAvVeileder();
        assertFeilkode(Feilkode.VARIGHET_FOR_LANG_MIDLERTIDIG_LONNSTILSKUDD, () -> avtale.forlengAvtale(avtale.getStartDato().plusMonths(24).plusDays(1), TestData.enNavIdent()));
    }

    @Test
    public void sommerjobb_må_være_godkjent_av_beslutter() {
        Avtale avtale = TestData.enSommerjobbAvtaleGodkjentAvVeileder();
        assertThat(avtale.statusSomEnum()).isEqualTo(Status.MANGLER_GODKJENNING);
        assertThat(avtale.getAvtaleInngått()).isNull();
        avtale.godkjennTilskuddsperiode(new NavIdent("B999999"), TestData.ENHET_OPPFØLGING.getVerdi());
        assertThat(avtale.statusSomEnum()).isEqualTo(Status.AVSLUTTET);
        assertThat(avtale.getAvtaleInngått()).isNotNull();
    }

    @Test
    public void godkjenn_tilskuddsperiode_feil_enhet() {
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
        Avtale avtale = TestData.enSommerjobbAvtaleGodkjentAvVeileder();

        // Kan ikke godkjenne når avtalen er tildelt seg selv
        assertFeilkode(Feilkode.TILSKUDDSPERIODE_IKKE_GODKJENNE_EGNE, () -> avtale.godkjennTilskuddsperiode(avtale.getGodkjentAvNavIdent(), "4444"));

        // Kan heller ikke godkjenne når avtalen er tildelt en annen
        avtale.overtaAvtale(new NavIdent("P887766"));
        assertFeilkode(Feilkode.TILSKUDDSPERIODE_IKKE_GODKJENNE_EGNE, () -> avtale.godkjennTilskuddsperiode(avtale.getGodkjentAvNavIdent(), "4444"));
    }

    @Test
    public void forleng_og_forkort_skal_redusere_prosent() {
        Avtale avtale = Avtale.veilederOppretterAvtale(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD), TestData.enNavIdent());
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setLonnstilskuddProsent(60);
        endreAvtale.setStartDato(Now.localDate());
        endreAvtale.setSluttDato(Now.localDate().plusMonths(12).minusDays(1));
        avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.noneOf(Tiltakstype.class));
        avtale.godkjennForDeltaker(TestData.etFodselsnummer());
        avtale.godkjennForArbeidsgiver(TestData.etFodselsnummer());
        avtale.godkjennForVeileder(TestData.enNavIdent());

        avtale.forlengAvtale(Now.localDate().plusMonths(12), TestData.enNavIdent());
        assertThat(avtale.getDatoForRedusertProsent()).isEqualTo(Now.localDate().plusMonths(12));
        assertThat(avtale.getSumLønnstilskuddRedusert()).isNotNull();

        avtale.forkortAvtale(Now.localDate().plusMonths(12).minusDays(1), "grunn", "", TestData.enNavIdent());
        assertThat(avtale.getDatoForRedusertProsent()).isNull();
        assertThat(avtale.getSumLønnstilskuddRedusert()).isNull();
    }
}
