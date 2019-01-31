package no.nav.tag.tiltaksgjennomforing.domene;

import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TilgangskontrollException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AvtalepartTest {
    @Test(expected = TilgangskontrollException.class)
    public void endreAvtale__skal_feile_for_deltaker() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Deltaker deltaker = TestData.enDeltaker(avtale);
        deltaker.endreAvtale(avtale, avtale.getVersjon(), TestData.ingenEndring());
    }

    @Test
    public void endreAvtale__skal_fungere_for_arbeidsgiver() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        arbeidsgiver.endreAvtale(avtale, avtale.getVersjon(), TestData.ingenEndring());
    }

    @Test
    public void endreAvtale__skal_fungere_for_veilder() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.endreAvtale(avtale, avtale.getVersjon(), TestData.ingenEndring());
    }

    @Test
    public void endreGodkjenning__skal_fungere_for_deltaker() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Deltaker deltaker = TestData.enDeltaker(avtale);
        deltaker.endreGodkjenning(avtale, true);
        assertThat(avtale.isGodkjentAvDeltaker()).isTrue();
        assertThat(avtale.isGodkjentAvArbeidsgiver()).isFalse();
        assertThat(avtale.isGodkjentAvVeileder()).isFalse();
    }

    @Test
    public void endreGodkjenning__skal_fungere_for_arbeidsgiver() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        arbeidsgiver.endreGodkjenning(avtale, true);
        assertThat(avtale.isGodkjentAvArbeidsgiver()).isTrue();
        assertThat(avtale.isGodkjentAvVeileder()).isFalse();
        assertThat(avtale.isGodkjentAvDeltaker()).isFalse();
    }

    @Test
    public void endreGodkjenning__skal_fungere_for_veileder() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.endreGodkjenning(avtale, true);
        assertThat(avtale.isGodkjentAvVeileder()).isTrue();
        assertThat(avtale.isGodkjentAvDeltaker()).isFalse();
        assertThat(avtale.isGodkjentAvArbeidsgiver()).isFalse();
    }

    @Test
    public void veilederSkalKunneEndreEgenGodkjenningTilbakeIgjen() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvVeileder(true);
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.endreGodkjenning(avtale, false);
        assertThat(avtale.isGodkjentAvVeileder()).isFalse();
    }
}