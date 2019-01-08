package no.nav.tag.tiltaksgjennomforing;

import no.nav.tag.tiltaksgjennomforing.domene.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;

public class AvtaleTest {

    @Test
    public void kunParteneIAvtalenSkalHaTilgang() {
        Bruker arbeidsgiver = new Bruker(new Fnr("77667766776"));
        Bruker kandidat = new Bruker(new Fnr("01234567890"));
        Veileder veileder = new Veileder(new NavIdent("X123456"));

        Avtale avtale = Avtale.nyAvtale(new OpprettAvtale(kandidat.getFnr()), veileder.getNavIdent());
        avtale.setArbeidsgiverFnr(arbeidsgiver.getFnr());

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
        NavIdent veilederNavIdent = new NavIdent("X123456");
        Avtale avtale = Avtale.nyAvtale(new OpprettAvtale(deltakerFnr), veilederNavIdent);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(avtale.getOpprettetTidspunkt()).isEqualToIgnoringMinutes(LocalDateTime.now());
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
            softly.assertThat(avtale.getArbeidsgiverFnr()).isNull();
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
        Avtale.nyAvtale(new OpprettAvtale(null), new NavIdent("X12345"));
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void nyAvtaleSkalFeileHvisManglerVeileder() {
        Avtale.nyAvtale(new OpprettAvtale(new Fnr("1122334455555")), null);
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
        avtale.endreAvtale(TestData.ingenEndring());
        assertThat(avtale.getVersjon()).isEqualTo(2);
    }
}