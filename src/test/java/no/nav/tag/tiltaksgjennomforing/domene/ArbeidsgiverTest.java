package no.nav.tag.tiltaksgjennomforing.domene;

import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TiltaksgjennomforingException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class ArbeidsgiverTest {
    @Test
    public void opphevGodkjenninger__kan_oppheve_ved_deltakergodkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvDeltaker(true);
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        arbeidsgiver.opphevGodkjenninger();
        assertThat(avtale.isGodkjentAvDeltaker()).isFalse();
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void opphevGodkjenninger__kan_ikke_oppheve_veiledergodkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvVeileder(true);
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        arbeidsgiver.opphevGodkjenninger();
    }
}