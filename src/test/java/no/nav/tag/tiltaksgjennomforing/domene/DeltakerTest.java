package no.nav.tag.tiltaksgjennomforing.domene;

import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TiltaksgjennomforingException;
import org.junit.Test;

public class DeltakerTest {
    @Test(expected = TiltaksgjennomforingException.class)
    public void opphevGodkjenninger__kan_aldri_oppheve_godkjenninger() {
        Deltaker deltaker = TestData.enDeltaker(TestData.enAvtaleMedAltUtfylt());
        deltaker.opphevGodkjenninger();
    }
}