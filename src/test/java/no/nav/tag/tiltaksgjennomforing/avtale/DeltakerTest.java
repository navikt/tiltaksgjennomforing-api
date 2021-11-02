package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.KanIkkeOppheveException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DeltakerTest {
    @Test
    public void opphevGodkjenninger__kan_aldri_oppheve_godkjenninger() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Deltaker deltaker = TestData.enDeltaker(avtale);
        assertThatThrownBy(() -> deltaker.opphevGodkjenninger(avtale)).isInstanceOf(KanIkkeOppheveException.class);
    }
}