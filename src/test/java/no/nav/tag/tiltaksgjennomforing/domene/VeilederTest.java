package no.nav.tag.tiltaksgjennomforing.domene;

import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TiltaksgjennomforingException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VeilederTest {
    @Test(expected = TiltaksgjennomforingException.class)
    public void godkjennAvtale__kan_ikke_godkjenne_foerst() {
        Veileder veileder = TestData.enVeileder(TestData.enAvtaleMedAltUtfylt());
        veileder.godkjennAvtale();
    }

    @Test
    public void godkjennAvtale__kan_godkjenne_sist() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvDeltaker(true);
        avtale.setGodkjentAvArbeidsgiver(true);
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.godkjennAvtale();
        assertThat(avtale.isGodkjentAvVeileder()).isTrue();
    }

    @Test
    public void opphevGodkjenninger__kan_alltid_oppheve_godkjenninger() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvVeileder(true);
        avtale.setGodkjentAvDeltaker(true);
        avtale.setGodkjentAvArbeidsgiver(true);
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.opphevGodkjenninger();

    }
}