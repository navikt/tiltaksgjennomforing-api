package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.autorisasjon.SlettemerkeProperties;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Tilgang;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging.VeilarboppfolgingService;
import no.nav.tag.tiltaksgjennomforing.exceptions.ArbeidsgiverSkalGodkjenneFørVeilederException;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.KanIkkeEndreException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;
import no.nav.tag.tiltaksgjennomforing.orgenhet.EregService;
import no.nav.tag.tiltaksgjennomforing.orgenhet.Organisasjon;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AvtalepartTest {

    @Test
    public void endreAvtale__skal_feile_for_deltaker() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Deltaker deltaker = TestData.enDeltaker(avtale);
        assertThatThrownBy(() -> deltaker.endreAvtale(TestData.ingenEndring(), avtale)).isInstanceOf(KanIkkeEndreException.class);
    }

    @Test
    public void godkjennForVeilederOgDeltaker__skal_feile_hvis_ag_ikke_har_godkjent() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        PersondataService persondataService = mock(PersondataService.class);

        Veileder veileder = new Veileder(
            avtale.getVeilederNavIdent(),
            null,
            tilgangskontrollService,
            persondataService,
            mock(Norg2Client.class),
            Set.of(new NavEnhet("4802", "Trysil")),
            mock(SlettemerkeProperties.class),
            TestData.INGEN_AD_GRUPPER,
            mock(VeilarboppfolgingService.class),
            mock(FeatureToggleService.class),
            mock(EregService.class)
        );

        when(tilgangskontrollService.hentSkrivetilgang(veileder, avtale.getDeltakerFnr())).thenReturn(new Tilgang.Tillat());

        GodkjentPaVegneGrunn godkjentPaVegneGrunn = TestData.enGodkjentPaVegneGrunn();
        assertThatThrownBy(() -> veileder.godkjennForVeilederOgDeltaker(godkjentPaVegneGrunn, avtale)).isInstanceOf(ArbeidsgiverSkalGodkjenneFørVeilederException.class);
    }

    @Test
    public void godkjennForVeileder__skal_feile_hvis_mentor_ikke_har_signert() {
        Avtale avtale = TestData.enMentorAvtaleUsignert();
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        PersondataService persondataService = mock(PersondataService.class);
        EregService eregService  = mock(EregService.class);

        Veileder veileder = new Veileder(
            avtale.getVeilederNavIdent(),
            null,
            tilgangskontrollService,
            persondataService,
            mock(Norg2Client.class),
            Set.of(new NavEnhet("4802", "Trysil")),
            mock(SlettemerkeProperties.class),
            TestData.INGEN_AD_GRUPPER,
            mock(VeilarboppfolgingService.class),
            mock(FeatureToggleService.class),
            eregService
        );

        when(tilgangskontrollService.hentSkrivetilgang(veileder, avtale.getDeltakerFnr())).thenReturn(new Tilgang.Tillat());
        when(eregService.hentVirksomhet(any())).thenReturn(new Organisasjon(TestData.etBedriftNr(), "Arbeidsplass AS"));

        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        arbeidsgiver.godkjennAvtale(avtale);
        Deltaker deltaker = TestData.enDeltaker(avtale);
        deltaker.godkjennAvtale(avtale);
        assertFeilkode(Feilkode.MENTOR_MÅ_SIGNERE_TAUSHETSERKLÆRING,() -> veileder.godkjennAvtale(avtale));
    }

    @Test
    public void godkjennForVeilederOgDeltaker__skal_feile_hvis_mentor_ikke_har_signert() {
        Avtale avtale = TestData.enMentorAvtaleUsignert();
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        PersondataService persondataService = mock(PersondataService.class);

        Veileder veileder = new Veileder(
            avtale.getVeilederNavIdent(),
            null,
            tilgangskontrollService,
            persondataService,
            mock(Norg2Client.class),
            Set.of(new NavEnhet("4802", "Trysil")),
            mock(SlettemerkeProperties.class),
            TestData.INGEN_AD_GRUPPER,
            mock(VeilarboppfolgingService.class),
            mock(FeatureToggleService.class),
            mock(EregService.class)
        );

        when(tilgangskontrollService.hentSkrivetilgang(veileder, avtale.getDeltakerFnr())).thenReturn(new Tilgang.Tillat());

        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        arbeidsgiver.godkjennAvtale(avtale);
        assertFeilkode(Feilkode.MENTOR_MÅ_SIGNERE_TAUSHETSERKLÆRING,() -> veileder.godkjennForVeilederOgDeltaker(new GodkjentPaVegneGrunn(), avtale));
    }

    @Test
    public void godkjennForVeilederOgDeltaker__skal_fungere_for_veileder() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        arbeidsgiver.godkjennForAvtalepart(avtale);
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        PersondataService persondataService = mock(PersondataService.class);

        Veileder veileder = new Veileder(
            avtale.getVeilederNavIdent(),
            null,
            tilgangskontrollService,
            persondataService,
            mock(Norg2Client.class),
            Set.of(new NavEnhet("4802", "Trysil")),
            mock(SlettemerkeProperties.class),
            TestData.INGEN_AD_GRUPPER,
            mock(VeilarboppfolgingService.class),
            mock(FeatureToggleService.class),
            mock(EregService.class)
        );

        when(tilgangskontrollService.hentSkrivetilgang(veileder, avtale.getDeltakerFnr())).thenReturn(new Tilgang.Tillat());

        GodkjentPaVegneGrunn godkjentPaVegneGrunn = TestData.enGodkjentPaVegneGrunn();
        veileder.godkjennForVeilederOgDeltaker(godkjentPaVegneGrunn, avtale);
        assertThat(avtale.erGodkjentAvDeltaker()).isTrue();
        assertThat(avtale.erGodkjentAvVeileder()).isTrue();
        assertThat(avtale.getGjeldendeInnhold().isGodkjentPaVegneAv()).isTrue();
    }

    @Test
    public void endreAvtale__skal_fungere_for_arbeidsgiver() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        arbeidsgiver.endreAvtale(TestData.ingenEndring(), avtale);
    }

    @Test
    public void endreAvtale__skal_fungere_for_veileder() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        PersondataService persondataService = mock(PersondataService.class);

        Veileder veileder = new Veileder(
            avtale.getVeilederNavIdent(),
            null,
            tilgangskontrollService,
            persondataService,
            mock(Norg2Client.class),
            Set.of(new NavEnhet("4802", "Trysil")),
            mock(SlettemerkeProperties.class),
            TestData.INGEN_AD_GRUPPER,
            mock(VeilarboppfolgingService.class),
            mock(FeatureToggleService.class),
            mock(EregService.class)
        );

        when(tilgangskontrollService.hentSkrivetilgang(veileder, avtale.getDeltakerFnr())).thenReturn(new Tilgang.Tillat());

        veileder.endreAvtale(TestData.ingenEndring(), avtale);
    }

    @Test
    public void godkjennForAvtalepart__skal_fungere_for_deltaker() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Deltaker deltaker = TestData.enDeltaker(avtale);
        deltaker.godkjennAvtale(avtale);
        assertThat(avtale.erGodkjentAvDeltaker()).isTrue();
        assertThat(avtale.erGodkjentAvArbeidsgiver()).isFalse();
        assertThat(avtale.erGodkjentAvVeileder()).isFalse();
    }

    @Test
    public void godkjennForAvtalepart__skal_fungere_for_arbeidsgiver() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        arbeidsgiver.godkjennAvtale(avtale);
        assertThat(avtale.erGodkjentAvArbeidsgiver()).isTrue();
        assertThat(avtale.erGodkjentAvVeileder()).isFalse();
        assertThat(avtale.erGodkjentAvDeltaker()).isFalse();
    }

    @Test
    public void godkjennForAvtalepart__skal_fungere_for_veileder() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        PersondataService persondataService = mock(PersondataService.class);
        EregService eregService  = mock(EregService.class);

        Veileder veileder = new Veileder(
            avtale.getVeilederNavIdent(),
            null,
            tilgangskontrollService,
            persondataService,
            mock(Norg2Client.class),
            Set.of(new NavEnhet("4802", "Trysil")),
            mock(SlettemerkeProperties.class),
            TestData.INGEN_AD_GRUPPER,
            mock(VeilarboppfolgingService.class),
            mock(FeatureToggleService.class),
            eregService
        );

        when(tilgangskontrollService.hentSkrivetilgang(veileder, avtale.getDeltakerFnr())).thenReturn(new Tilgang.Tillat());
        when(eregService.hentVirksomhet(any())).thenReturn(new Organisasjon(TestData.etBedriftNr(), "Arbeidsplass AS"));

        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        veileder.godkjennAvtale(avtale);
        assertThat(avtale.erGodkjentAvArbeidsgiver()).isTrue();
    }

    @Test
    public void opphevGodkjenninger__veileder_skal_kunne_trekke_tilbake_egen_godkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.opphevGodkjenninger(avtale);
        assertThat(avtale.erGodkjentAvVeileder()).isFalse();
    }

    @Test
    public void opphevGodkjenninger__feiler_hvis_alle_har_allerede_godkjent_og_avtale_er_inngått() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        arbeidsgiver.godkjennForAvtalepart(avtale);
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        PersondataService persondataService = mock(PersondataService.class);

        Veileder veileder = new Veileder(
            avtale.getVeilederNavIdent(),
            null,
            tilgangskontrollService,
            persondataService,
            mock(Norg2Client.class),
            Set.of(new NavEnhet("4802", "Trysil")),
            mock(SlettemerkeProperties.class),
            TestData.INGEN_AD_GRUPPER,
            mock(VeilarboppfolgingService.class),
            mock(FeatureToggleService.class),
            mock(EregService.class)
        );

        when(tilgangskontrollService.hentSkrivetilgang(veileder, avtale.getDeltakerFnr())).thenReturn(new Tilgang.Tillat());
        GodkjentPaVegneGrunn godkjentPaVegneGrunn = TestData.enGodkjentPaVegneGrunn();
        veileder.godkjennForVeilederOgDeltaker(godkjentPaVegneGrunn, avtale);
        assertThat(avtale.erGodkjentAvDeltaker()).isTrue();
        assertThat(avtale.erGodkjentAvVeileder()).isTrue();
        assertThat(avtale.erGodkjentAvArbeidsgiver()).isTrue();
        assertThat(avtale.getGjeldendeInnhold().isGodkjentPaVegneAv()).isTrue();
        assertFeilkode(Feilkode.KAN_IKKE_OPPHEVE_GODKJENNINGER_VED_INNGAATT_AVTALE, () -> veileder.opphevGodkjenninger(avtale));
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
