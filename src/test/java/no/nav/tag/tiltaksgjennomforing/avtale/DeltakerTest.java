package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.KanIkkeOppheveException;
import org.junit.Test;

public class DeltakerTest {
    @Test(expected = KanIkkeOppheveException.class)
    public void opphevGodkjenninger__kan_aldri_oppheve_godkjenninger() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Deltaker deltaker = TestData.enDeltaker(avtale);
        deltaker.opphevGodkjenninger(avtale);
    }
}