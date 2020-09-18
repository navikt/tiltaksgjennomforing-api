package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.Deltaker;
import no.nav.tag.tiltaksgjennomforing.exceptions.KanIkkeOppheveException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import org.junit.Test;

public class DeltakerTest {
    @Test(expected = KanIkkeOppheveException.class)
    public void opphevGodkjenninger__kan_aldri_oppheve_godkjenninger() {
        Deltaker deltaker = TestData.enDeltaker(TestData.enAvtaleMedAltUtfylt());
        deltaker.opphevGodkjenninger();
    }
}