package no.nav.tag.tiltaksgjennomforing.domene;

import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetNavAnsatt;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetSelvbetjeningBruker;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.SamtidigeEndringerException;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TiltaksgjennomforingException;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AvtaleTest {

    @Test
    public void kunParteneIAvtalenSkalHaTilgang() {
        Deltaker deltaker = TestData.enDeltaker();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver();
        Veileder veileder = TestData.enVeileder();

        Avtale avtale = Avtale.nyAvtale(new OpprettAvtale(deltaker.getIdentifikator(), arbeidsgiver.getIdentifikator(), "Testbutikken"), veileder.getIdentifikator());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(avtale.harLesetilgang(TestData.innloggetSelvbetjeningBruker(arbeidsgiver))).isTrue();
            softly.assertThat(avtale.harLesetilgang(TestData.innloggetSelvbetjeningBruker(deltaker))).isTrue();
            softly.assertThat(avtale.harLesetilgang(TestData.innloggetNavAnsatt(veileder))).isTrue();
            softly.assertThat(avtale.harLesetilgang(TestData.enSelvbetjeningBruker())).isFalse();
            softly.assertThat(avtale.harLesetilgang(TestData.enNavAnsatt())).isFalse();
        });
    }

    @Test
    public void nyAvtaleFactorySkalReturnereRiktigeStandardverdier() {
        Fnr deltakerFnr = new Fnr("01234567890");
        Fnr arbeidsgiverFnr = new Fnr("12345678901");
        NavIdent veilederNavIdent = new NavIdent("X123456");
        Avtale avtale = Avtale.nyAvtale(new OpprettAvtale(deltakerFnr, arbeidsgiverFnr, "Testbedrift"), veilederNavIdent);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(avtale.getOpprettetTidspunkt()).isNull();
            softly.assertThat(avtale.getDeltakerFnr()).isEqualTo(deltakerFnr);
            softly.assertThat(avtale.getMaal()).isEmpty();
            softly.assertThat(avtale.getOppgaver()).isEmpty();
            softly.assertThat(avtale.getDeltakerFornavn()).isNull();
            softly.assertThat(avtale.getDeltakerEtternavn()).isNull();
            softly.assertThat(avtale.getBedriftNavn()).isEqualTo("Testbedrift");
            softly.assertThat(avtale.getBedriftNr()).isNull();
            softly.assertThat(avtale.getArbeidsgiverFnr()).isEqualTo(arbeidsgiverFnr);
            softly.assertThat(avtale.getArbeidsgiverFornavn()).isNull();
            softly.assertThat(avtale.getArbeidsgiverEtternavn()).isNull();
            softly.assertThat(avtale.getArbeidsgiverTlf()).isNull();
            softly.assertThat(avtale.getVeilederFornavn()).isNull();
            softly.assertThat(avtale.getVeilederEtternavn()).isNull();
            softly.assertThat(avtale.getVeilederTlf()).isNull();
            softly.assertThat(avtale.getOppfolging()).isNull();
            softly.assertThat(avtale.getTilrettelegging()).isNull();
            softly.assertThat(avtale.getStartDato()).isNull();
            softly.assertThat(avtale.getArbeidstreningLengde()).isNull();
            softly.assertThat(avtale.getArbeidstreningStillingprosent()).isNull();
            softly.assertThat(avtale.isGodkjentAvDeltaker()).isFalse();
            softly.assertThat(avtale.isGodkjentAvArbeidsgiver()).isFalse();
            softly.assertThat(avtale.isGodkjentAvVeileder()).isFalse();
        });
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void nyAvtaleSkalFeileHvisManglerDeltaker() {
        Avtale.nyAvtale(new OpprettAvtale(null, new Fnr("12345678901"), ""), new NavIdent("X12345"));
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void nyAvtaleSkalFeileHvisManglerArbeidsgiver() {
        Avtale.nyAvtale(new OpprettAvtale(new Fnr("12345678901"), null, ""), new NavIdent("X12345"));
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void nyAvtaleSkalFeileHvisManglerVeileder() {
        Avtale.nyAvtale(new OpprettAvtale(new Fnr("11223344555"), new Fnr("12345678901"), ""), null);
    }

    @Test(expected = SamtidigeEndringerException.class)
    public void sjekkVersjon__ugyldig_versjon() {
        Avtale avtale = TestData.enAvtale();
        avtale.sjekkVersjon(-1);
    }

    @Test(expected = SamtidigeEndringerException.class)
    public void sjekkVersjon__null() {
        Avtale avtale = TestData.enAvtale();
        avtale.sjekkVersjon(null);
    }

    @Test
    public void sjekkVersjon__gyldig_versjon() {
        Avtale avtale = TestData.enAvtale();
        avtale.sjekkVersjon(avtale.getVersjon());
    }

    @Test
    public void endreAvtaleSkalOppdatereRiktigeFelt() {
        Avtale avtale = TestData.enAvtale();
        EndreAvtale endreAvtale = TestData.endringPaAlleFelt();
        avtale.endreAvtale(avtale.getVersjon(), endreAvtale);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(avtale.getDeltakerFornavn()).isEqualTo(endreAvtale.getDeltakerFornavn());
            softly.assertThat(avtale.getDeltakerEtternavn()).isEqualTo(endreAvtale.getDeltakerEtternavn());
            softly.assertThat(avtale.getBedriftNavn()).isEqualTo(endreAvtale.getBedriftNavn());
            softly.assertThat(avtale.getBedriftNr()).isEqualTo(endreAvtale.getBedriftNr());
            softly.assertThat(avtale.getArbeidsgiverFornavn()).isEqualTo(endreAvtale.getArbeidsgiverFornavn());
            softly.assertThat(avtale.getArbeidsgiverEtternavn()).isEqualTo(endreAvtale.getArbeidsgiverEtternavn());
            softly.assertThat(avtale.getArbeidsgiverTlf()).isEqualTo(endreAvtale.getArbeidsgiverTlf());
            softly.assertThat(avtale.getVeilederFornavn()).isEqualTo(endreAvtale.getVeilederFornavn());
            softly.assertThat(avtale.getVeilederEtternavn()).isEqualTo(endreAvtale.getVeilederEtternavn());
            softly.assertThat(avtale.getVeilederTlf()).isEqualTo(endreAvtale.getVeilederTlf());
            softly.assertThat(avtale.getOppfolging()).isEqualTo(endreAvtale.getOppfolging());
            softly.assertThat(avtale.getTilrettelegging()).isEqualTo(endreAvtale.getTilrettelegging());
            softly.assertThat(avtale.getStartDato()).isEqualTo(endreAvtale.getStartDato());
            softly.assertThat(avtale.getArbeidstreningLengde()).isEqualTo(endreAvtale.getArbeidstreningLengde());
            softly.assertThat(avtale.getArbeidstreningStillingprosent()).isEqualTo(endreAvtale.getArbeidstreningStillingprosent());
            softly.assertThat(avtale.getMaal()).isEqualTo(endreAvtale.getMaal());
            softly.assertThat(avtale.getOppgaver()).isEqualTo(endreAvtale.getOppgaver());
        });
    }

    @Test
    public void endreAvtaleSkalIkkeEndreGodkjenninger() {
        Avtale avtale = TestData.enAvtale();
        boolean deltakerGodkjenningFoerEndring = avtale.isGodkjentAvDeltaker();
        boolean arbeidsgiverGodkjenningFoerEndring = avtale.isGodkjentAvArbeidsgiver();
        boolean veilederGodkjenningFoerEndring = avtale.isGodkjentAvVeileder();

        avtale.endreAvtale(avtale.getVersjon(), TestData.endringPaAlleFelt());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(deltakerGodkjenningFoerEndring).isEqualTo(avtale.isGodkjentAvDeltaker());
            softly.assertThat(arbeidsgiverGodkjenningFoerEndring).isEqualTo(avtale.isGodkjentAvArbeidsgiver());
            softly.assertThat(veilederGodkjenningFoerEndring).isEqualTo(avtale.isGodkjentAvVeileder());
        });
    }

    @Test
    public void endreAvtaleSkalInkrementereVersjon() {
        Avtale avtale = TestData.enAvtale();
        avtale.endreAvtale(avtale.getVersjon(), TestData.ingenEndring());
        assertThat(avtale.getVersjon()).isEqualTo(2);
    }

    @Test
    public void deltakerKnyttetTilAvtaleSkalHaDeltakerRolle() {
        Avtale avtale = TestData.enAvtale();
        InnloggetSelvbetjeningBruker selvbetjeningBruker = TestData.innloggetSelvbetjeningBruker(TestData.enDeltaker(avtale));
        assertThat(avtale.hentAvtalepart(selvbetjeningBruker)).isInstanceOf(Deltaker.class);
    }

    @Test
    public void arbeidsgiverKnyttetTilAvtaleSkalHaArbeidsgiverRolle() {
        Avtale avtale = TestData.enAvtale();
        InnloggetSelvbetjeningBruker selvbetjeningBruker = TestData.innloggetSelvbetjeningBruker(TestData.enArbeidsgiver(avtale));
        assertThat(avtale.hentAvtalepart(selvbetjeningBruker)).isInstanceOf(Arbeidsgiver.class);
    }

    @Test
    public void veilederKnyttetTilAvtaleSkalHaVeilederRolle() {
        Avtale avtale = TestData.enAvtale();
        InnloggetNavAnsatt navAnsatt = TestData.innloggetNavAnsatt(TestData.enVeileder(avtale));
        assertThat(avtale.hentAvtalepart(navAnsatt)).isInstanceOf(Veileder.class);
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void kanIkkeGodkjennesNaarIkkeAltErUtfylt() {
        Avtale avtale = TestData.enAvtale();
        avtale.sjekkOmKanGodkjennes();
    }

    @Test
    public void kanGodkjennesNaarAltErUtfylt() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.sjekkOmKanGodkjennes();
    }
}