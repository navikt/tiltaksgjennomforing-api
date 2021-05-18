package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.ArbeidsgiverSkalGodkjenneFørVeilederException;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.KanIkkeEndreException;
import no.nav.tag.tiltaksgjennomforing.exceptions.SamtidigeEndringerException;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.EnumSet;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;
import static org.assertj.core.api.Assertions.assertThat;

public class AvtalepartTest {
    @Test(expected = KanIkkeEndreException.class)
    public void endreAvtale__skal_feile_for_deltaker() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Deltaker deltaker = TestData.enDeltaker(avtale);
        deltaker.endreAvtale(avtale.getSistEndret(), TestData.ingenEndring(), avtale, EnumSet.of(avtale.getTiltakstype()));
    }

    @Test(expected = ArbeidsgiverSkalGodkjenneFørVeilederException.class)
    public void godkjennForVeilederOgDeltaker__skal_feile_hvis_ag_ikke_har_godkjent() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Veileder veileder = TestData.enVeileder(avtale);
        GodkjentPaVegneGrunn godkjentPaVegneGrunn = TestData.enGodkjentPaVegneGrunn();
        veileder.godkjennForVeilederOgDeltaker(godkjentPaVegneGrunn, avtale);
    }

    @Test
    public void godkjennForVeilederOgDeltaker__skal_fungere_for_veileder() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        arbeidsgiver.godkjennForAvtalepart(avtale);
        Veileder veileder = TestData.enVeileder(avtale);
        GodkjentPaVegneGrunn godkjentPaVegneGrunn = TestData.enGodkjentPaVegneGrunn();
        veileder.godkjennForVeilederOgDeltaker(godkjentPaVegneGrunn, avtale);
        assertThat(avtale.erGodkjentAvDeltaker()).isTrue();
        assertThat(avtale.erGodkjentAvVeileder()).isTrue();
        assertThat(avtale.isGodkjentPaVegneAv()).isTrue();
    }

    @Test
    public void endreAvtale__skal_fungere_for_arbeidsgiver() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        arbeidsgiver.endreAvtale(Instant.now(), TestData.ingenEndring(), avtale, EnumSet.of(avtale.getTiltakstype()));
    }

    @Test
    public void endreAvtale__skal_fungere_for_veileder() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.endreAvtale(Instant.now(), TestData.ingenEndring(), avtale, EnumSet.of(avtale.getTiltakstype()));
    }

    @Test(expected = SamtidigeEndringerException.class)
    public void godkjennForAvtalepart__skal_ikke_fungere_hvis_versjon_er_feil() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Deltaker deltaker = TestData.enDeltaker(avtale);
        deltaker.godkjennAvtale(avtale.getSistEndret().minusMillis(1), avtale);
    }

    @Test
    public void godkjennForAvtalepart__skal_fungere_for_deltaker() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Deltaker deltaker = TestData.enDeltaker(avtale);
        deltaker.godkjennAvtale(avtale.getSistEndret(), avtale);
        assertThat(avtale.erGodkjentAvDeltaker()).isTrue();
        assertThat(avtale.erGodkjentAvArbeidsgiver()).isFalse();
        assertThat(avtale.erGodkjentAvVeileder()).isFalse();
    }

    @Test
    public void godkjennForAvtalepart__skal_fungere_for_arbeidsgiver() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        arbeidsgiver.godkjennAvtale(avtale.getSistEndret(), avtale);
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
        veileder.godkjennAvtale(avtale.getSistEndret(), avtale);
        assertThat(avtale.erGodkjentAvArbeidsgiver()).isTrue();
    }

    @Test
    public void opphevGodkjenninger__veileder_skal_kunne_trekke_tilbake_egen_godkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvVeileder(LocalDateTime.now());
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.opphevGodkjenninger(avtale);
        assertThat(avtale.erGodkjentAvVeileder()).isFalse();
    }

    @Test
    public void opphevGodkjenninger__feiler_hvis_ingen_har_godkjent() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Veileder veileder = TestData.enVeileder(avtale);
        assertFeilkode(Feilkode.KAN_IKKE_OPPHEVE, () -> veileder.opphevGodkjenninger(avtale));
    }

    @Test
    public void opphevGodkjenninger__kan_ikke_utfores_flere_ganger_etter_hverandre() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        arbeidsgiver.godkjennForAvtalepart(avtale);

        arbeidsgiver.opphevGodkjenninger(avtale);
        assertFeilkode(Feilkode.KAN_IKKE_OPPHEVE, () -> arbeidsgiver.opphevGodkjenninger(avtale));
    }
}