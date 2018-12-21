package no.nav.tag.tiltaksgjennomforing;

import no.nav.tag.tiltaksgjennomforing.domene.Avtale;
import no.nav.tag.tiltaksgjennomforing.domene.Fnr;
import no.nav.tag.tiltaksgjennomforing.domene.NavIdent;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class AvtaleTest {

    @Test
    public void nyAvtaleFactorySkalReturnereRiktigeStandardverdier() {
        Fnr deltakerFnr = new Fnr("01234567890");
        NavIdent veilederNavIdent = new NavIdent("X123456");
        Avtale avtale = Avtale.nyAvtale(deltakerFnr, veilederNavIdent);
        assertThat(avtale.getOpprettetTidspunkt()).isEqualToIgnoringMinutes(LocalDateTime.now());
        assertThat(avtale.getDeltakerFnr()).isEqualTo(deltakerFnr);
        assertThat(avtale.getMaal()).isEmpty();
        assertThat(avtale.getOppgaver()).isEmpty();
        assertThat(avtale.getDeltakerFornavn()).isNull();
        assertThat(avtale.getDeltakerEtternavn()).isNull();
        assertThat(avtale.getDeltakerAdresse()).isNull();
        assertThat(avtale.getDeltakerPostnummer()).isNull();
        assertThat(avtale.getDeltakerPoststed()).isNull();
        assertThat(avtale.getBedriftNavn()).isNull();
        assertThat(avtale.getBedriftAdresse()).isNull();
        assertThat(avtale.getBedriftPostnummer()).isNull();
        assertThat(avtale.getBedriftPoststed()).isNull();
        assertThat(avtale.getArbeidsgiverFnr()).isNull();
        assertThat(avtale.getArbeidsgiverFornavn()).isNull();
        assertThat(avtale.getArbeidsgiverEtternavn()).isNull();
        assertThat(avtale.getArbeidsgiverEpost()).isNull();
        assertThat(avtale.getArbeidsgiverTlf()).isNull();
        assertThat(avtale.getVeilederFornavn()).isNull();
        assertThat(avtale.getVeilederEtternavn()).isNull();
        assertThat(avtale.getVeilederEpost()).isNull();
        assertThat(avtale.getVeilederTlf()).isNull();
        assertThat(avtale.getOppfolging()).isNull();
        assertThat(avtale.getTilrettelegging()).isNull();
        assertThat(avtale.getStartDatoTidspunkt()).isNull();
        assertThat(avtale.getArbeidstreningLengde()).isNull();
        assertThat(avtale.getArbeidstreningStillingprosent()).isNull();
        assertThat(avtale.isBekreftetAvBruker()).isFalse();
        assertThat(avtale.isBekreftetAvArbeidsgiver()).isFalse();
        assertThat(avtale.isBekreftetAvVeileder()).isFalse();
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void nyAvtaleSkalFeileHvisManglerDeltaker() {
        Avtale.nyAvtale(null, new NavIdent("X12345"));
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void nyAvtaleSkalFeileHvisManglerVeileder() {
        Avtale.nyAvtale(new Fnr("1122334455555"), null);
    }
}