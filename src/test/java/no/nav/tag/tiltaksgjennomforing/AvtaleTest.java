package no.nav.tag.tiltaksgjennomforing;

import no.nav.tag.tiltaksgjennomforing.domene.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;

public class AvtaleTest {

    @Test
    public void kunParteneIAvtalenSkalHaTilgang() {
        Bruker arbeidsgiver = new Bruker(new Fnr("77667766776"));
        Bruker kandidat = new Bruker(new Fnr("12345678901"));
        Veileder veileder = new Veileder(new NavIdent("X123456"));

        Avtale avtale = Avtale.nyAvtale(new OpprettAvtale(kandidat.getFnr(), arbeidsgiver.getFnr()), veileder.getNavIdent());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(avtale.erTilgjengeligFor(arbeidsgiver)).isTrue();
            softly.assertThat(avtale.erTilgjengeligFor(kandidat)).isTrue();
            softly.assertThat(avtale.erTilgjengeligFor(veileder)).isTrue();
            softly.assertThat(avtale.erTilgjengeligFor(new Bruker(new Fnr("90909090909")))).isFalse();
            softly.assertThat(avtale.erTilgjengeligFor(new Veileder(new NavIdent("Y654321")))).isFalse();
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
            softly.assertThat(avtale.isBekreftetAvBruker()).isFalse();
            softly.assertThat(avtale.isBekreftetAvArbeidsgiver()).isFalse();
            softly.assertThat(avtale.isBekreftetAvVeileder()).isFalse();
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
        Avtale avtale = TestData.minimalAvtale();
        avtale.sjekkVersjon(-1);
    }

    @Test
    public void sjekkVersjonMedGyldigVersjon() {
        Avtale avtale = TestData.minimalAvtale();
        avtale.sjekkVersjon(avtale.getVersjon());
    }

    @Test
    public void endreAvtaleSkalInkrementereVersjon() {
        Avtale avtale = TestData.minimalAvtale();
        avtale.endreAvtale(avtale.getVersjon(), TestData.ingenEndring());
        assertThat(avtale.getVersjon()).isEqualTo(2);
    }

    @Test
    public void deltakerKnyttetTilAvtaleSkalHaDeltakerRolle() {
        Avtale avtale = TestData.minimalAvtale();
        Bruker deltaker = new Bruker(avtale.getDeltakerFnr());
        assertThat(avtale.hentRolle(deltaker)).isEqualTo(Rolle.DELTAKER);
    }

    @Test
    public void arbeidsgiverKnyttetTilAvtaleSkalHaArbeidsgiverRolle() {
        Avtale avtale = TestData.minimalAvtale();
        Bruker arbeidsgiver = new Bruker(avtale.getArbeidsgiverFnr());
        assertThat(avtale.hentRolle(arbeidsgiver)).isEqualTo(Rolle.ARBEIDSGIVER);
    }

    @Test
    public void veilederKnyttetTilAvtaleSkalHaVeilederRolle() {
        Avtale avtale = TestData.minimalAvtale();
        Veileder veileder = new Veileder(avtale.getVeilederNavIdent());
        assertThat(avtale.hentRolle(veileder)).isEqualTo(Rolle.VEILEDER);
    }

    @Test
    public void personUtenTilgangTilAvtaleSkalHaIngenRolle() {
        Avtale avtale = TestData.minimalAvtale();
        Bruker deltakerUtenTilgang = new Bruker("00000000000");
        assertThat(avtale.hentRolle(deltakerUtenTilgang)).isEqualTo(Rolle.INGEN_ROLLE);
    }
}