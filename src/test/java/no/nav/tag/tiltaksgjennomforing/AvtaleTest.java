package no.nav.tag.tiltaksgjennomforing;

import no.nav.tag.tiltaksgjennomforing.controller.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.domene.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import static no.nav.tag.tiltaksgjennomforing.domene.Rolle.INGEN_ROLLE;
import static org.assertj.core.api.Assertions.assertThat;

public class AvtaleTest {

    @Test
    public void kunParteneIAvtalenSkalHaTilgang() {
        Bruker deltaker = TestData.deltaker();
        Bruker arbeidsgiver = TestData.arbeidsgiver();
        Veileder veileder = TestData.veileder();

        Avtale avtale = Avtale.nyAvtale(new OpprettAvtale(deltaker.getFnr(), arbeidsgiver.getFnr()), veileder.getNavIdent());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(avtale.erTilgjengeligFor(arbeidsgiver)).isTrue();
            softly.assertThat(avtale.erTilgjengeligFor(deltaker)).isTrue();
            softly.assertThat(avtale.erTilgjengeligFor(veileder)).isTrue();
            softly.assertThat(avtale.erTilgjengeligFor(TestData.deltakerUtenTilgang())).isFalse();
            softly.assertThat(avtale.erTilgjengeligFor(TestData.veilederUtenTilgang())).isFalse();
        });
    }

    @Test
    public void nyAvtaleFactorySkalReturnereRiktigeStandardverdier() {
        Fnr deltakerFnr = new Fnr("01234567890");
        Fnr arbeidsgiverFnr = new Fnr("12345678901");
        NavIdent veilederNavIdent = new NavIdent("X123456");
        Avtale avtale = Avtale.nyAvtale(new OpprettAvtale(deltakerFnr, arbeidsgiverFnr), veilederNavIdent);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(avtale.getOpprettetTidspunkt()).isNull();
            softly.assertThat(avtale.getDeltakerFnr()).isEqualTo(deltakerFnr);
            softly.assertThat(avtale.getMaal()).isEmpty();
            softly.assertThat(avtale.getOppgaver()).isEmpty();
            softly.assertThat(avtale.getDeltakerFornavn()).isNull();
            softly.assertThat(avtale.getDeltakerEtternavn()).isNull();
            softly.assertThat(avtale.getDeltakerAdresse()).isNull();
            softly.assertThat(avtale.getDeltakerPostnummer()).isNull();
            softly.assertThat(avtale.getDeltakerPoststed()).isNull();
            softly.assertThat(avtale.getBedriftNavn()).isNull();
            softly.assertThat(avtale.getBedriftAdresse()).isNull();
            softly.assertThat(avtale.getBedriftPostnummer()).isNull();
            softly.assertThat(avtale.getBedriftPoststed()).isNull();
            softly.assertThat(avtale.getArbeidsgiverFnr()).isEqualTo(arbeidsgiverFnr);
            softly.assertThat(avtale.getArbeidsgiverFornavn()).isNull();
            softly.assertThat(avtale.getArbeidsgiverEtternavn()).isNull();
            softly.assertThat(avtale.getArbeidsgiverEpost()).isNull();
            softly.assertThat(avtale.getArbeidsgiverTlf()).isNull();
            softly.assertThat(avtale.getVeilederFornavn()).isNull();
            softly.assertThat(avtale.getVeilederEtternavn()).isNull();
            softly.assertThat(avtale.getVeilederEpost()).isNull();
            softly.assertThat(avtale.getVeilederTlf()).isNull();
            softly.assertThat(avtale.getOppfolging()).isNull();
            softly.assertThat(avtale.getTilrettelegging()).isNull();
            softly.assertThat(avtale.getStartDatoTidspunkt()).isNull();
            softly.assertThat(avtale.getArbeidstreningLengde()).isNull();
            softly.assertThat(avtale.getArbeidstreningStillingprosent()).isNull();
            softly.assertThat(avtale.isGodkjentAvDeltaker()).isFalse();
            softly.assertThat(avtale.isGodkjentAvArbeidsgiver()).isFalse();
            softly.assertThat(avtale.isGodkjentAvVeileder()).isFalse();
        });
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void nyAvtaleSkalFeileHvisManglerDeltaker() {
        Avtale.nyAvtale(new OpprettAvtale(null, new Fnr("12345678901")), new NavIdent("X12345"));
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void nyAvtaleSkalFeileHvisManglerArbeidsgiver() {
        Avtale.nyAvtale(new OpprettAvtale(new Fnr("12345678901"), null), new NavIdent("X12345"));
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void nyAvtaleSkalFeileHvisManglerVeileder() {
        Avtale.nyAvtale(new OpprettAvtale(new Fnr("11223344555"), new Fnr("12345678901")), null);
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void sjekkVersjonMedUgyldigVersjon() {
        Avtale avtale = TestData.enAvtale();
        avtale.sjekkVersjon(-1);
    }

    @Test
    public void sjekkVersjonMedGyldigVersjon() {
        Avtale avtale = TestData.enAvtale();
        avtale.sjekkVersjon(avtale.getVersjon());
    }

    @Test
    public void endreAvtaleSkalOppdatereRiktigeFelt() {
        Avtale avtale = TestData.enAvtale();
        EndreAvtale endreAvtale = TestData.endringPaAlleFelt();
        avtale.endreAvtale(avtale.getVersjon(), TestData.arbeidsgiver(avtale), endreAvtale);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(avtale.getDeltakerFornavn()).isEqualTo(endreAvtale.getDeltakerFornavn());
            softly.assertThat(avtale.getDeltakerEtternavn()).isEqualTo(endreAvtale.getDeltakerEtternavn());
            softly.assertThat(avtale.getDeltakerAdresse()).isEqualTo(endreAvtale.getDeltakerAdresse());
            softly.assertThat(avtale.getDeltakerPostnummer()).isEqualTo(endreAvtale.getDeltakerPostnummer());
            softly.assertThat(avtale.getDeltakerPoststed()).isEqualTo(endreAvtale.getDeltakerPoststed());
            softly.assertThat(avtale.getBedriftNavn()).isEqualTo(endreAvtale.getBedriftNavn());
            softly.assertThat(avtale.getBedriftAdresse()).isEqualTo(endreAvtale.getBedriftAdresse());
            softly.assertThat(avtale.getBedriftPostnummer()).isEqualTo(endreAvtale.getBedriftPostnummer());
            softly.assertThat(avtale.getBedriftPoststed()).isEqualTo(endreAvtale.getBedriftPoststed());
            softly.assertThat(avtale.getArbeidsgiverFornavn()).isEqualTo(endreAvtale.getArbeidsgiverFornavn());
            softly.assertThat(avtale.getArbeidsgiverEtternavn()).isEqualTo(endreAvtale.getArbeidsgiverEtternavn());
            softly.assertThat(avtale.getArbeidsgiverEpost()).isEqualTo(endreAvtale.getArbeidsgiverEpost());
            softly.assertThat(avtale.getArbeidsgiverTlf()).isEqualTo(endreAvtale.getArbeidsgiverTlf());
            softly.assertThat(avtale.getVeilederFornavn()).isEqualTo(endreAvtale.getVeilederFornavn());
            softly.assertThat(avtale.getVeilederEtternavn()).isEqualTo(endreAvtale.getVeilederEtternavn());
            softly.assertThat(avtale.getVeilederEpost()).isEqualTo(endreAvtale.getVeilederEpost());
            softly.assertThat(avtale.getVeilederTlf()).isEqualTo(endreAvtale.getVeilederTlf());
            softly.assertThat(avtale.getOppfolging()).isEqualTo(endreAvtale.getOppfolging());
            softly.assertThat(avtale.getTilrettelegging()).isEqualTo(endreAvtale.getTilrettelegging());
            softly.assertThat(avtale.getStartDatoTidspunkt()).isEqualTo(endreAvtale.getStartDatoTidspunkt());
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

        avtale.endreAvtale(avtale.getVersjon(), TestData.arbeidsgiver(avtale), TestData.endringPaAlleFelt());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(deltakerGodkjenningFoerEndring).isEqualTo(avtale.isGodkjentAvDeltaker());
            softly.assertThat(arbeidsgiverGodkjenningFoerEndring).isEqualTo(avtale.isGodkjentAvArbeidsgiver());
            softly.assertThat(veilederGodkjenningFoerEndring).isEqualTo(avtale.isGodkjentAvVeileder());
        });
    }

    @Test
    public void endreAvtaleSkalInkrementereVersjon() {
        Avtale avtale = TestData.enAvtale();
        avtale.endreAvtale(avtale.getVersjon(), TestData.veileder(avtale), TestData.ingenEndring());
        assertThat(avtale.getVersjon()).isEqualTo(2);
    }

    @Test
    public void deltakerKnyttetTilAvtaleSkalHaDeltakerRolle() {
        Avtale avtale = TestData.enAvtale();
        Bruker deltaker = TestData.deltaker(avtale);
        assertThat(avtale.hentRollenTil(deltaker)).isEqualTo(Rolle.DELTAKER);
    }

    @Test
    public void arbeidsgiverKnyttetTilAvtaleSkalHaArbeidsgiverRolle() {
        Avtale avtale = TestData.enAvtale();
        Bruker arbeidsgiver = TestData.arbeidsgiver(avtale);
        assertThat(avtale.hentRollenTil(arbeidsgiver)).isEqualTo(Rolle.ARBEIDSGIVER);
    }

    @Test
    public void veilederKnyttetTilAvtaleSkalHaVeilederRolle() {
        Avtale avtale = TestData.enAvtale();
        Veileder veileder = TestData.veileder(avtale);
        assertThat(avtale.hentRollenTil(veileder)).isEqualTo(Rolle.VEILEDER);
    }

    @Test
    public void personUtenTilgangTilAvtaleSkalHaIngenRolle() {
        Avtale avtale = TestData.enAvtale();
        Rolle rolle = avtale.hentRollenTil(TestData.deltakerUtenTilgang());

        assertThat(rolle).isEqualTo(INGEN_ROLLE);
    }

    @Test(expected = TilgangskontrollException.class)
    public void endreAvtaleSkalKasteTilgangskontrollExceptionHvisPersonSomEndrerErDeltaker() {
        Avtale avtale = TestData.enAvtale();
        avtale.endreAvtale(avtale.getVersjon(), TestData.deltaker(avtale), TestData.ingenEndring());
    }

    @Test
    public void arbeidsgiverSkalKunneEndreAvtale() {
        Avtale avtale = TestData.enAvtale();
        avtale.endreAvtale(avtale.getVersjon(), TestData.arbeidsgiver(avtale), TestData.ingenEndring());
    }

    @Test
    public void veilederSkalKunneEndreAvtale() {
        Avtale avtale = TestData.enAvtale();
        avtale.endreAvtale(avtale.getVersjon(), TestData.veileder(avtale), TestData.ingenEndring());
    }

    @Test
    public void deltakerSkalKunneEndreEgenGodkjenning() {
        Avtale avtale = TestData.enAvtale();
        Bruker deltaker = TestData.deltaker(avtale);
        avtale.endreGodkjenning(deltaker, true);
        assertThat(avtale.isGodkjentAvDeltaker()).isTrue();
    }

    @Test
    public void arbeidsgiverSkalKunneEndreEgenGodkjenning() {
        Avtale avtale = TestData.enAvtale();
        Bruker arbeidsgiver = TestData.arbeidsgiver(avtale);
        avtale.endreGodkjenning(arbeidsgiver, true);
        assertThat(avtale.isGodkjentAvArbeidsgiver()).isTrue();
    }

    @Test
    public void veilederSkalKunneEndreEgenGodkjenning() {
        Avtale avtale = TestData.enAvtale();
        Veileder veileder = TestData.veileder(avtale);
        avtale.endreGodkjenning(veileder, true);
        assertThat(avtale.isGodkjentAvVeileder()).isTrue();
    }

    @Test
    public void skalIkkeKunneGodkjennePaVegneAvAndreEnnSegSelv() {
        Avtale avtale = TestData.enAvtale();
        Veileder veileder = TestData.veileder(avtale);
        avtale.endreGodkjenning(veileder, true);
        assertThat(avtale.isGodkjentAvDeltaker()).isFalse();
    }
}