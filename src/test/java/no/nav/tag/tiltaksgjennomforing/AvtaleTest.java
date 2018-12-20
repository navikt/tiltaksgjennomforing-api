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
        assertThat(avtale.getDeltakerFornavn()).isEqualTo("");
        assertThat(avtale.getDeltakerEtternavn()).isEqualTo("");
        assertThat(avtale.getDeltakerAdresse()).isEqualTo("");
        assertThat(avtale.getDeltakerPostnummer()).isEqualTo("");
        assertThat(avtale.getDeltakerPoststed()).isEqualTo("");
        assertThat(avtale.getBedriftNavn()).isEqualTo("");
        assertThat(avtale.getBedriftAdresse()).isEqualTo("");
        assertThat(avtale.getBedriftPostnummer()).isEqualTo("");
        assertThat(avtale.getBedriftPoststed()).isEqualTo("");
        assertThat(avtale.getArbeidsgiverFnr()).isEqualTo(null);
        assertThat(avtale.getArbeidsgiverFornavn()).isEqualTo("");
        assertThat(avtale.getArbeidsgiverEtternavn()).isEqualTo("");
        assertThat(avtale.getArbeidsgiverEpost()).isEqualTo("");
        assertThat(avtale.getArbeidsgiverTlf()).isEqualTo("");
        assertThat(avtale.getVeilederFornavn()).isEqualTo("");
        assertThat(avtale.getVeilederEtternavn()).isEqualTo("");
        assertThat(avtale.getVeilederEpost()).isEqualTo("");
        assertThat(avtale.getVeilederTlf()).isEqualTo("");
        assertThat(avtale.getOppfolging()).isEqualTo("");
        assertThat(avtale.getTilrettelegging()).isEqualTo("");
        assertThat(avtale.getStartDatoTidspunkt()).isEqualToIgnoringMinutes(LocalDateTime.now());
        assertThat(avtale.getArbeidstreningLengde()).isEqualTo(1);
        assertThat(avtale.getArbeidstreningStillingprosent()).isEqualTo(0);
        assertThat(avtale.isBekreftetAvBruker()).isFalse();
        assertThat(avtale.isBekreftetAvArbeidsgiver()).isFalse();
        assertThat(avtale.isBekreftetAvVeileder()).isFalse();
    }
}