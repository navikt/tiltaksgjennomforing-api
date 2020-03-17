package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.TestData;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import org.junit.Ignore;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class VeilederTest {
    @Test(expected = TiltaksgjennomforingException.class)
    public void godkjennAvtale__kan_ikke_godkjenne_foerst() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.godkjennAvtale(avtale.getSistEndret());
    }

    @Test
    public void godkjennAvtale__kan_godkjenne_sist() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.godkjennAvtale(avtale.getSistEndret());
        assertThat(avtale.erGodkjentAvVeileder()).isTrue();
    }

    @Test
    public void opphevGodkjenninger__kan_alltid_oppheve_godkjenninger() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvVeileder(LocalDateTime.now());
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.opphevGodkjenninger();
        assertThat(avtale.erGodkjentAvDeltaker()).isFalse();
        assertThat(avtale.erGodkjentAvArbeidsgiver()).isFalse();
        assertThat(avtale.erGodkjentAvVeileder()).isFalse();
    }

    @Test
    @Ignore
    public void avbrytAvtale__kan_ikke_avbryt_avtale_etter_veiledergodkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvVeileder(LocalDateTime.now());
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        Veileder veileder = TestData.enVeileder(avtale);
        //veileder.avbrytAvtale(avtale.getSistEndret());
        assertThat(avtale.isAvbrutt()).isFalse();
    }

    @Test
    @Ignore
    public void avbrytAvtale__kan_avbryt_avtale_foer_veiledergodkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        Veileder veileder = TestData.enVeileder(avtale);
        //veileder.avbrytAvtale(avtale.getSistEndret());
        assertThat(avtale.isAvbrutt()).isTrue();
    }
}
