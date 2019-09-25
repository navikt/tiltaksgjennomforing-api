package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.TestData;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class AvtalepartTest {
    @Test(expected = TilgangskontrollException.class)
    public void endreAvtale__skal_feile_for_deltaker() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Deltaker deltaker = TestData.enDeltaker(avtale);
        deltaker.endreAvtale(avtale.getVersjon(), TestData.ingenEndring());
    }

    @Test(expected = TilgangskontrollException.class)
    public void godkjennForVeilederOgDeltaker__skal_feile_for_deltaker() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Deltaker deltaker = TestData.enDeltaker(avtale);
        GodkjentPaVegneGrunn godkjentPaVegneGrunn = TestData.enGodkjentPaVegneGrunn();
        deltaker.godkjennForVeilederOgDeltaker(godkjentPaVegneGrunn);
    }

    @Test(expected = TilgangskontrollException.class)
    public void godkjennForVeilederOgDeltaker__skal_feile_for_arbeidsgiver() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        GodkjentPaVegneGrunn godkjentPaVegneGrunn = TestData.enGodkjentPaVegneGrunn();
        arbeidsgiver.godkjennForVeilederOgDeltaker(godkjentPaVegneGrunn);
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void godkjennForVeilederOgDeltaker__skal_feile_hvis_ag_ikke_har_godkjent() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Veileder veileder = TestData.enVeileder(avtale);
        GodkjentPaVegneGrunn godkjentPaVegneGrunn = TestData.enGodkjentPaVegneGrunn();
        veileder.godkjennForVeilederOgDeltaker(godkjentPaVegneGrunn);
    }

    @Test
    public void godkjennForVeilederOgDeltaker__skal_fungere_for_veileder() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        arbeidsgiver.godkjennForAvtalepart();
        Veileder veileder = TestData.enVeileder(avtale);
        GodkjentPaVegneGrunn godkjentPaVegneGrunn = TestData.enGodkjentPaVegneGrunn();
        veileder.godkjennForVeilederOgDeltaker(godkjentPaVegneGrunn);
        assertThat(avtale.erGodkjentAvDeltaker()).isTrue();
        assertThat(avtale.erGodkjentAvVeileder()).isTrue();
        assertThat(avtale.isGodkjentPaVegneAv()).isTrue();
    }

    @Test
    public void endreAvtale__skal_fungere_for_arbeidsgiver() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        arbeidsgiver.endreAvtale(avtale.getVersjon(), TestData.ingenEndring());
    }

    @Test
    public void endreAvtale__skal_fungere_for_veileder() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.endreAvtale(avtale.getVersjon(), TestData.ingenEndring());
    }

    @Test(expected = SamtidigeEndringerException.class)
    public void godkjennForAvtalepart__skal_ikke_fungere_hvis_versjon_er_feil() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Deltaker deltaker = TestData.enDeltaker(avtale);
        deltaker.godkjennAvtale(-1);
    }

    @Test
    public void godkjennForAvtalepart__skal_fungere_for_deltaker() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Deltaker deltaker = TestData.enDeltaker(avtale);
        deltaker.godkjennAvtale(avtale.getVersjon());
        assertThat(avtale.erGodkjentAvDeltaker()).isTrue();
        assertThat(avtale.erGodkjentAvArbeidsgiver()).isFalse();
        assertThat(avtale.erGodkjentAvVeileder()).isFalse();
    }

    @Test
    public void godkjennForAvtalepart__skal_fungere_for_arbeidsgiver() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        arbeidsgiver.godkjennAvtale(avtale.getVersjon());
        assertThat(avtale.erGodkjentAvArbeidsgiver()).isTrue();
        assertThat(avtale.erGodkjentAvVeileder()).isFalse();
        assertThat(avtale.erGodkjentAvDeltaker()).isFalse();
    }

    @Test
    public void godkjennForAvtalepart__skal_fungere_for_veileder() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.godkjennAvtale(avtale.getVersjon());
        assertThat(avtale.erGodkjentAvArbeidsgiver()).isTrue();
    }

    @Test
    public void opphevGodkjenninger__veileder_skal_kunne_trekke_tilbake_egen_godkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvVeileder(LocalDateTime.now());
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.opphevGodkjenninger();
        assertThat(avtale.erGodkjentAvVeileder()).isFalse();
    }
}