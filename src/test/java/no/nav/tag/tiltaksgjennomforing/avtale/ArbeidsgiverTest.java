package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.TestData;
import no.nav.tag.tiltaksgjennomforing.exceptions.KanIkkeOppheveException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;


public class ArbeidsgiverTest {
    @Test
    public void opphevGodkjenninger__kan_oppheve_ved_deltakergodkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        arbeidsgiver.opphevGodkjenninger(avtale);
        assertThat(avtale.erGodkjentAvDeltaker()).isFalse();
    }

    @Test(expected = KanIkkeOppheveException.class)
    public void opphevGodkjenninger__kan_ikke_oppheve_veiledergodkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvVeileder(LocalDateTime.now());
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        arbeidsgiver.opphevGodkjenninger(avtale);
    }
}