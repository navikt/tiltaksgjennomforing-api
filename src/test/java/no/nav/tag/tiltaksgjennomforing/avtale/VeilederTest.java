package no.nav.tag.tiltaksgjennomforing.avtale;

import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.SlettemerkeProperties;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Tilgang;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.enhet.Formidlingsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2GeoResponse;
import no.nav.tag.tiltaksgjennomforing.enhet.Oppfølgingsstatus;
import no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging.VeilarboppfolgingService;
import no.nav.tag.tiltaksgjennomforing.exceptions.ErAlleredeVeilederException;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.IkkeAdminTilgangException;
import no.nav.tag.tiltaksgjennomforing.exceptions.Kode6SperretForOpprettelseOgEndringException;
import no.nav.tag.tiltaksgjennomforing.exceptions.VeilederSkalGodkjenneSistException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggle;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;
import no.nav.tag.tiltaksgjennomforing.orgenhet.EregService;
import no.nav.tag.tiltaksgjennomforing.orgenhet.Organisasjon;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import no.nav.team_tiltak.felles.persondata.pdl.domene.Diskresjonskode;
import no.nav.team_tiltak.felles.persondata.pdl.domene.Navn;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;
import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.featureToggleService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VeilederTest {

    @BeforeEach
    public void setup() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;
    }

    @AfterEach
    public void tearDown() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;
    }

    @Test
    public void godkjennAvtale__kan_ikke_godkjenne_foerst() {
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

        assertThatThrownBy(() -> veileder.godkjennAvtale(avtale))
                .isExactlyInstanceOf(VeilederSkalGodkjenneSistException.class);
    }

    @Test
    public void godkjennAvtale__kan_godkjenne_sist() {
        // GITT
        Avtale avtale = TestData.enVarigLonnstilskuddAvtaleMedAltUtfylt();
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS);
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.instant());
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.instant());

        VeilarboppfolgingService veilarboppfolgingService = mock(VeilarboppfolgingService.class);
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
            veilarboppfolgingService,
            mock(FeatureToggleService.class),
            eregService
        );

        when(tilgangskontrollService.hentSkrivetilgang(veileder, avtale.getDeltakerFnr())).thenReturn(new Tilgang.Tillat());
        when(eregService.hentVirksomhet(any())).thenReturn(new Organisasjon(TestData.etBedriftNr(), "Arbeidsplass AS"));
        when(persondataService.hentDiskresjonskode(avtale.getDeltakerFnr())).thenReturn(Diskresjonskode.STRENGT_FORTROLIG);

        // NÅR
        when(veilarboppfolgingService.hentOgSjekkOppfolgingstatus(avtale)).thenReturn(new Oppfølgingsstatus(Formidlingsgruppe.ARBEIDSSOKER, Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS, "0906"));
        veileder.godkjennAvtale(avtale);

        // SÅ
        assertThat(avtale.erGodkjentAvVeileder()).isTrue();
        assertThat(avtale.getKvalifiseringsgruppe().getKvalifiseringskode()).isEqualTo(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS.getKvalifiseringskode());
    }

    @Test
    public void godkjennForVeilederOgDeltaker__kan_godkjenne_med_riktig_oppfølgingsstatus() {
        // GITT
        Avtale avtale = TestData.enVarigLonnstilskuddAvtaleMedAltUtfylt();
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS);
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.instant());
        VeilarboppfolgingService veilarboppfolgingService = mock(VeilarboppfolgingService.class);

        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        PersondataService persondataService = mock(PersondataService.class);
        when(persondataService.hentDiskresjonskode(avtale.getDeltakerFnr())).thenReturn(Diskresjonskode.STRENGT_FORTROLIG);
        Veileder veileder = new Veileder(
            avtale.getVeilederNavIdent(),
            null,
            tilgangskontrollService,
            persondataService,
            mock(Norg2Client.class),
            Set.of(new NavEnhet("4802", "Trysil")),
            mock(SlettemerkeProperties.class),
            TestData.INGEN_AD_GRUPPER,
            veilarboppfolgingService,
            mock(FeatureToggleService.class),
            mock(EregService.class)
        );
        when(tilgangskontrollService.hentSkrivetilgang(veileder, avtale.getDeltakerFnr())).thenReturn(new Tilgang.Tillat());
        // NÅR
        when(veilarboppfolgingService.hentOgSjekkOppfolgingstatus(avtale)).thenReturn(new Oppfølgingsstatus(Formidlingsgruppe.ARBEIDSSOKER, Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS, "0906"));
        GodkjentPaVegneGrunn godkjentPaVegneGrunn = TestData.enGodkjentPaVegneGrunn();
        veileder.godkjennForVeilederOgDeltaker(godkjentPaVegneGrunn, avtale);

        // SÅ
        assertThat(avtale.erGodkjentAvVeileder()).isTrue();
        assertThat(avtale.getKvalifiseringsgruppe().getKvalifiseringskode()).isEqualTo(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS.getKvalifiseringskode());
    }

    @Test
    public void godkjennForVeilederOgDeltakerOgArbeidsgiver__kan_godkjenne_med_riktig_oppfølgingsstatus() {
        // GITT
        Avtale avtale = TestData.enVarigLonnstilskuddAvtaleMedAltUtfylt();
        avtale.setOpphav(Avtaleopphav.ARENA);
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS);
        VeilarboppfolgingService veilarboppfolgingService = mock(VeilarboppfolgingService.class);
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        PersondataService persondataService = mock(PersondataService.class);
        when(persondataService.hentDiskresjonskode(avtale.getDeltakerFnr())).thenReturn(Diskresjonskode.STRENGT_FORTROLIG);

        Veileder veileder = new Veileder(
            avtale.getVeilederNavIdent(),
            null,
            tilgangskontrollService,
            persondataService,
            mock(Norg2Client.class),
            Set.of(new NavEnhet("4802", "Trysil")),
            mock(SlettemerkeProperties.class),
            TestData.INGEN_AD_GRUPPER,
            veilarboppfolgingService,
            mock(FeatureToggleService.class),
            mock(EregService.class)
        );

        when(tilgangskontrollService.hentSkrivetilgang(veileder, avtale.getDeltakerFnr())).thenReturn(new Tilgang.Tillat());

        // NÅR
        when(veilarboppfolgingService.hentOgSjekkOppfolgingstatus(avtale)).thenReturn(new Oppfølgingsstatus(Formidlingsgruppe.ARBEIDSSOKER, Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS, "0906"));
        GodkjentPaVegneAvDeltakerOgArbeidsgiverGrunn godkjentPaVegneAvDeltakerOgArbeidsgiverGrunn = TestData.enGodkjentPaVegneAvDeltakerOgArbeidsgiverGrunn();
        veileder.godkjennForVeilederOgDeltakerOgArbeidsgiver(godkjentPaVegneAvDeltakerOgArbeidsgiverGrunn, avtale);

        // SÅ
        assertThat(avtale.erGodkjentAvVeileder()).isTrue();
        assertThat(avtale.getKvalifiseringsgruppe().getKvalifiseringskode()).isEqualTo(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS.getKvalifiseringskode());
    }

    @Test
    public void godkjennForVeilederOgArbeidsgiver__kan_godkjenne_med_riktig_oppfølgingsstatus() {
        // GITT
        Avtale avtale = TestData.enVarigLonnstilskuddAvtaleMedAltUtfylt();
        avtale.setOpphav(Avtaleopphav.ARENA);
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS);
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.instant());
        VeilarboppfolgingService veilarboppfolgingService = mock(VeilarboppfolgingService.class);
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        PersondataService persondataService = mock(PersondataService.class);
        when(persondataService.hentDiskresjonskode(avtale.getDeltakerFnr())).thenReturn(Diskresjonskode.STRENGT_FORTROLIG);

        Veileder veileder = new Veileder(
            avtale.getVeilederNavIdent(),
            null,
            tilgangskontrollService,
            persondataService,
            mock(Norg2Client.class),
            Set.of(new NavEnhet("4802", "Trysil")),
            mock(SlettemerkeProperties.class),
            TestData.INGEN_AD_GRUPPER,
            veilarboppfolgingService,
            mock(FeatureToggleService.class),
            mock(EregService.class)
        );

        when(tilgangskontrollService.hentSkrivetilgang(veileder, avtale.getDeltakerFnr())).thenReturn(new Tilgang.Tillat());

        // NÅR
        when(veilarboppfolgingService.hentOgSjekkOppfolgingstatus(avtale)).thenReturn(new Oppfølgingsstatus(Formidlingsgruppe.ARBEIDSSOKER, Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS, "0906"));
        GodkjentPaVegneAvArbeidsgiverGrunn godkjentPaVegneGrunn = TestData.enGodkjentPaVegneAvArbeidsgiverGrunn();
        veileder.godkjennForVeilederOgArbeidsgiver(godkjentPaVegneGrunn, avtale);

        // SÅ
        assertThat(avtale.erGodkjentAvVeileder()).isTrue();
        assertThat(avtale.getKvalifiseringsgruppe().getKvalifiseringskode()).isEqualTo(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS.getKvalifiseringskode());
    }

    @Test
    public void godkjennAvtale__kan_ikke_godkjenne_kode6_med_togglet_adressesperresjekk() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.instant());
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.instant());
        when(featureToggleService.isEnabled(FeatureToggle.KODE_6_SPERRE)).thenReturn(true);
        PersondataService persondataService = mock(PersondataService.class);
        when(persondataService.hentDiskresjonskode(avtale.getDeltakerFnr())).thenReturn(Diskresjonskode.STRENGT_FORTROLIG);

        VeilarboppfolgingService veilarboppfolgingService = mock(VeilarboppfolgingService.class);
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        when(persondataService.hentDiskresjonskode(avtale.getDeltakerFnr())).thenReturn(Diskresjonskode.STRENGT_FORTROLIG);

        Veileder veileder = new Veileder(
            avtale.getVeilederNavIdent(),
            null,
            tilgangskontrollService,
            persondataService,
            mock(Norg2Client.class),
            Set.of(new NavEnhet("4802", "Trysil")),
            mock(SlettemerkeProperties.class),
            TestData.INGEN_AD_GRUPPER,
            veilarboppfolgingService,
            featureToggleService,
            mock(EregService.class)
        );

        when(tilgangskontrollService.hentSkrivetilgang(veileder, avtale.getDeltakerFnr())).thenReturn(new Tilgang.Tillat());
        assertThatThrownBy(() -> veileder.godkjennAvtale(avtale))
                .isExactlyInstanceOf(Kode6SperretForOpprettelseOgEndringException.class);
    }

    @Test
    public void godkjennAvtale__kan_godkjenne_kode6_uten_togglet_adressesperresjekk() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.instant());
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.instant());
        when(featureToggleService.isEnabled(FeatureToggle.KODE_6_SPERRE)).thenReturn(false);
        PersondataService persondataService = mock(PersondataService.class);
        when(persondataService.hentDiskresjonskode(avtale.getDeltakerFnr())).thenReturn(Diskresjonskode.STRENGT_FORTROLIG);
        VeilarboppfolgingService veilarboppfolgingService = mock(VeilarboppfolgingService.class);
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
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
            veilarboppfolgingService,
            featureToggleService,
            eregService
        );

        when(tilgangskontrollService.hentSkrivetilgang(veileder, avtale.getDeltakerFnr())).thenReturn(new Tilgang.Tillat());
        when(eregService.hentVirksomhet(any())).thenReturn(new Organisasjon(TestData.etBedriftNr(), "Arbeidsplass AS"));

        veileder.godkjennAvtale(avtale);
        assertThat(avtale.erGodkjentAvVeileder()).isTrue();
    }

    @Test
    public void godkjennForVeilederOgDeltaker__kan_ikke_godkjenne_kode6_med_togglet_adressesperresjekk() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.instant());
        when(featureToggleService.isEnabled(FeatureToggle.KODE_6_SPERRE)).thenReturn(true);
        PersondataService persondataService = mock(PersondataService.class);
        when(persondataService.hentDiskresjonskode(avtale.getDeltakerFnr())).thenReturn(Diskresjonskode.STRENGT_FORTROLIG);
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);

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
            featureToggleService,
            mock(EregService.class)
        );
        when(tilgangskontrollService.hentSkrivetilgang(veileder, avtale.getDeltakerFnr())).thenReturn(new Tilgang.Tillat());
        assertThatThrownBy(() -> veileder.godkjennForVeilederOgDeltaker(TestData.enGodkjentPaVegneGrunn(), avtale))
                .isExactlyInstanceOf(Kode6SperretForOpprettelseOgEndringException.class);
    }

    @Test
    public void godkjennForVeilederOgDeltaker__kan_godkjenne_kode6_uten_togglet_adressesperresjekk() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        when(featureToggleService.isEnabled(FeatureToggle.KODE_6_SPERRE)).thenReturn(false);
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.instant());
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        PersondataService persondataService = mock(PersondataService.class);
        when(persondataService.hentDiskresjonskode(avtale.getDeltakerFnr())).thenReturn(Diskresjonskode.STRENGT_FORTROLIG);

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
        veileder.godkjennForVeilederOgDeltaker(TestData.enGodkjentPaVegneGrunn(), avtale);
        assertThat(avtale.erGodkjentAvVeileder()).isTrue();
    }

    @Test
    public void opphevGodkjenninger__kan_ikke_oppheve_godkjenninger_når_avtale_er_inngått() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.instant());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.instant());
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.instant());
        avtale.getGjeldendeInnhold().setAvtaleInngått(Now.instant());
        Veileder veileder = TestData.enVeileder(avtale);
        assertFeilkode(
                Feilkode.KAN_IKKE_OPPHEVE_GODKJENNINGER_VED_INNGAATT_AVTALE,
                () -> veileder.opphevGodkjenninger(avtale)
        );
    }

    @Test
    public void opphevGodkjenninger__kan_oppheve_godkjenninger_hvis_alle_parter_har_godkjent_men_ikke_inngått() {
        Now.fixedDate(LocalDate.of(2021, 6, 1));
        Avtale avtale = TestData.enSommerjobbAvtaleGodkjentAvVeileder();
        Veileder veileder = TestData.enVeileder(avtale);
        assertThat(
                avtale.godkjentAvArbeidsgiver() != null &&
                        avtale.godkjentAvDeltaker() != null &&
                        avtale.godkjentAvVeileder() != null
        ).isTrue();
        assertThat(avtale.erAvtaleInngått()).isFalse();

        veileder.opphevGodkjenninger(avtale);
        assertThat(
                avtale.godkjentAvArbeidsgiver() == null &&
                        avtale.godkjentAvDeltaker() == null &&
                        avtale.godkjentAvVeileder() == null
        ).isTrue();
        Now.resetClock();
    }

    @Test
    public void opphevGodkjenninger__kan_ikke_oppheve_arbeidstrening_hvis_alle_parter_har_godkjent() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);

        VeilarboppfolgingService veilarboppfolgingService = mock(VeilarboppfolgingService.class);
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        PersondataService persondataService = mock(PersondataService.class);
        when(persondataService.hentDiskresjonskode(avtale.getDeltakerFnr())).thenReturn(Diskresjonskode.STRENGT_FORTROLIG);

        Veileder veileder = new Veileder(
            avtale.getVeilederNavIdent(),
            null,
            tilgangskontrollService,
            persondataService,
            mock(Norg2Client.class),
            Set.of(new NavEnhet("4802", "Trysil")),
            mock(SlettemerkeProperties.class),
            TestData.INGEN_AD_GRUPPER,
            veilarboppfolgingService,
            mock(FeatureToggleService.class),
            mock(EregService.class)
        );
        avtale.endreAvtale(
                TestData.endringPåAlleArbeidstreningFelter(),
                Avtalerolle.VEILEDER
        );

        when(tilgangskontrollService.hentSkrivetilgang(veileder, avtale.getDeltakerFnr())).thenReturn(new Tilgang.Tillat());
        arbeidsgiver.godkjennAvtale(avtale);
        veileder.godkjennForVeilederOgDeltaker(TestData.enGodkjentPaVegneGrunn(), avtale);

        assertFeilkode(
                Feilkode.KAN_IKKE_OPPHEVE_GODKJENNINGER_VED_INNGAATT_AVTALE,
                () -> veileder.opphevGodkjenninger(avtale)
        );
    }

    @Test
    public void opphevgodkjenninger__kan_ikke_oppheve_hvis_første_tilskuddsperiode_er_godkjent() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);

        // Gi veileder tilgang til deltaker
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        VeilarboppfolgingService veilarboppfolgingServiceMock = mock(VeilarboppfolgingService.class);
        PersondataService persondataService = mock(PersondataService.class);
        FeatureToggleService featureToggleServiceMock = mock(FeatureToggleService.class);
        when(veilarboppfolgingServiceMock.hentOgSjekkOppfolgingstatus(avtale)).thenReturn(new Oppfølgingsstatus(Formidlingsgruppe.ARBEIDSSOKER, Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS, "0906"));
        when(persondataService.hentDiskresjonskode(any(Fnr.class))).thenReturn(Diskresjonskode.UGRADERT);
        Veileder veileder = new Veileder(
                avtale.getVeilederNavIdent(),
                null,
                tilgangskontrollService,
                persondataService,
                mock(Norg2Client.class),
                Set.of(new NavEnhet("4802", "Trysil")),
                mock(SlettemerkeProperties.class),
                TestData.INGEN_AD_GRUPPER,
                veilarboppfolgingServiceMock,
                featureToggleServiceMock,
                mock(EregService.class)
        );
        when(tilgangskontrollService.hentSkrivetilgang(eq(veileder), any(Fnr.class))).thenReturn(new Tilgang.Tillat());

        avtale.endreAvtale(
                TestData.endringPåAlleLønnstilskuddFelter(),
                Avtalerolle.VEILEDER
        );
        arbeidsgiver.godkjennAvtale(avtale);
        veileder.godkjennForVeilederOgDeltaker(TestData.enGodkjentPaVegneGrunn(), avtale);

        assertThat(avtale.erAvtaleInngått()).isFalse();

        veileder.opphevGodkjenninger(avtale);
        arbeidsgiver.godkjennAvtale(avtale);
        veileder.godkjennForVeilederOgDeltaker(TestData.enGodkjentPaVegneGrunn(), avtale);

        Beslutter beslutter = TestData.enBeslutter(avtale);
        beslutter.godkjennTilskuddsperiode(avtale, "0000");

        assertThat(avtale.erAvtaleInngått()).isTrue();
        assertFeilkode(
                Feilkode.KAN_IKKE_OPPHEVE_GODKJENNINGER_VED_INNGAATT_AVTALE,
                () -> veileder.opphevGodkjenninger(avtale)
        );
    }


    @Test
    public void annullerAvtale__kan_annuller_avtale_etter_veiledergodkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.instant());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.instant());
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.instant());
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.annullerAvtale("enGrunn", avtale);
        assertThat(avtale.getAnnullertTidspunkt()).isNotNull();
        assertThat(avtale.getAnnullertGrunn()).isEqualTo("enGrunn");
    }

    @Test
    public void annullerAvtale__kan_annullere_avtale_foer_veiledergodkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.instant());
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.instant());
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.annullerAvtale("enGrunn", avtale);
        assertThat(avtale.getAnnullertTidspunkt()).isNotNull();
        assertThat(avtale.getAnnullertGrunn()).isEqualTo("enGrunn");
    }

    @Test
    public void overtarAvtale() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        NavIdent gammelVeileder = avtale.getVeilederNavIdent();
        Veileder nyVeileder = TestData.enVeileder(new NavIdent("J987654"));
        nyVeileder.overtaAvtale(avtale);
        assertThat(gammelVeileder).isNotEqualTo(nyVeileder.getIdentifikator());
        assertThat(avtale.getVeilederNavIdent()).isEqualTo(nyVeileder.getIdentifikator());
    }

    @Test
    public void overta_avtale_hvor_veileder_allerede_er_satt_og_skal_bare_overskrive_oppfølgningsstatus_når_avtalen_endres() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();

        VeilarboppfolgingService veilarboppfolgingService = Mockito.spy(new VeilarboppfolgingService(null));
        Veileder nyVeileder = TestData.enVeileder(new NavIdent("J987654"),veilarboppfolgingService);

        Oppfølgingsstatus nyOppfølgingsstatusSomSkalIkkeSettes = new Oppfølgingsstatus(
                Formidlingsgruppe.ARBEIDSSOKER,
                Kvalifiseringsgruppe.SPESIELT_TILPASSET_INNSATS,
                "0906"
        );
        Mockito.doReturn(nyOppfølgingsstatusSomSkalIkkeSettes).when(veilarboppfolgingService).hentOppfolgingsstatus(
            anyString());

        nyVeileder.hentOppfølgingFraArena(avtale,veilarboppfolgingService );

        assertThat(avtale.getKvalifiseringsgruppe()).isEqualTo(avtale.getKvalifiseringsgruppe());

        //SKal kunne endre oppfølgningsstatus på endre avtale
        Oppfølgingsstatus nyOppfølgingsstatusSomSkalSettes = new Oppfølgingsstatus(
                Formidlingsgruppe.ARBEIDSSOKER,
                Kvalifiseringsgruppe.SPESIELT_TILPASSET_INNSATS,
                "0906"
        );
        Mockito.doReturn(nyOppfølgingsstatusSomSkalSettes).when(veilarboppfolgingService).hentOppfolgingsstatus(
            anyString());
        nyVeileder.oppdatereOppfølgingStatusVedEndreAvtale(avtale);
        assertThat(avtale.getKvalifiseringsgruppe()).isEqualTo(nyOppfølgingsstatusSomSkalSettes.getKvalifiseringsgruppe());
    }

    @Test
    public void overtarAvtale_uten_tilskuddsprosent__verifiser_blir_satt_og_beregnet() {
        Avtale avtale = Avtale.opprett(
                new OpprettAvtale(
                        TestData.etFodselsnummer(),
                        TestData.etBedriftNr(),
                        Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD
                ),
                Avtaleopphav.ARBEIDSGIVER
        );

        VeilarboppfolgingService veilarboppfolgingService = mock(VeilarboppfolgingService.class);
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        PersondataService persondataService = mock(PersondataService.class);
        when(persondataService.hentDiskresjonskode(avtale.getDeltakerFnr())).thenReturn(Diskresjonskode.STRENGT_FORTROLIG);


        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        endreAvtale.setLonnstilskuddProsent(null);
        avtale.getGjeldendeInnhold().setSumLonnstilskudd(null);
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        arbeidsgiver.endreAvtale(endreAvtale, avtale);

        Veileder nyVeileder = new Veileder(
            new NavIdent("J987654"),
            null,
            tilgangskontrollService,
            persondataService,
            mock(Norg2Client.class),
            Set.of(new NavEnhet("4802", "Trysil")),
            mock(SlettemerkeProperties.class),
            TestData.INGEN_AD_GRUPPER,
            veilarboppfolgingService,
            mock(FeatureToggleService.class),
            mock(EregService.class)
        );

        when(tilgangskontrollService.hentSkrivetilgang(nyVeileder, avtale.getDeltakerFnr())).thenReturn(new Tilgang.Tillat());

        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS);
        avtale.setFormidlingsgruppe(Formidlingsgruppe.ARBEIDSSOKER);
        avtale.getGjeldendeInnhold().setLonnstilskuddProsent(avtale.getKvalifiseringsgruppe()
                .finnLonntilskuddProsentsatsUtifraKvalifiseringsgruppe(40, 60));
        assertThat(avtale.getGjeldendeInnhold().getSumLonnstilskudd()).isNull();

        nyVeileder.overtaAvtale(avtale);

        assertThat(avtale.getGjeldendeInnhold().getSumLonnstilskudd()).isNotNull();
    }

    @Test
    public void overtarAvtale__feil_hvis_samme_ident() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();

        VeilarboppfolgingService veilarboppfolgingService = mock(VeilarboppfolgingService.class);
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        PersondataService persondataService = mock(PersondataService.class);
        when(persondataService.hentDiskresjonskode(avtale.getDeltakerFnr())).thenReturn(Diskresjonskode.STRENGT_FORTROLIG);

        Veileder veileder = new Veileder(
            avtale.getVeilederNavIdent(),
            null,
            tilgangskontrollService,
            persondataService,
            mock(Norg2Client.class),
            Set.of(new NavEnhet("4802", "Trysil")),
            mock(SlettemerkeProperties.class),
            TestData.INGEN_AD_GRUPPER,
            veilarboppfolgingService,
            mock(FeatureToggleService.class),
            mock(EregService.class)
        );

        when(tilgangskontrollService.hentSkrivetilgang(veileder, avtale.getDeltakerFnr())).thenReturn(new Tilgang.Tillat());


        assertThatThrownBy(() -> veileder.overtaAvtale(avtale)).isExactlyInstanceOf(ErAlleredeVeilederException.class);
    }

    @Test
    public void overtaAvtale__skal_genere_tilskuddsperioder_hvis_ufordelt() {
        Avtale avtale = TestData.enAvtaleOpprettetAvArbeidsgiver(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        arbeidsgiver.endreAvtale(
                TestData.endringPåAlleLønnstilskuddFelter(),
                avtale
        );

        assertThat(avtale.getTilskuddPeriode()).isEmpty();

        Veileder veileder = TestData.enVeileder(new NavIdent("Z123456"));

        //Tilsvarende operasjon som gjøres fra endepunketet overta avtalecontrolleren
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS);
        avtale.getGjeldendeInnhold().setLonnstilskuddProsent(60);
        veileder.overtaAvtale(avtale);

        assertThat(avtale.getTilskuddPeriode()).isNotEmpty();




    }

    @Test
    public void oprettAvtale__setter_startverdier_på_avtale() {
        NavIdent navIdent = new NavIdent("Q987654");
        NavEnhet navEnhet = TestData.ENHET_GEOGRAFISK;
        OpprettAvtale opprettAvtale = new OpprettAvtale(
                TestData.etFodselsnummer(),
                TestData.etBedriftNr(),
                Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD
        );

        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        PersondataService persondataService = mock(PersondataService.class);
        Norg2Client norg2Client = mock(Norg2Client.class);
        VeilarboppfolgingService veilarboppfolgingService = mock(VeilarboppfolgingService.class);
        FeatureToggleService featureToggleServiceMock = mock(FeatureToggleService.class);
        EregService eregService  = mock(EregService.class);

        Veileder veileder = new Veileder(
                navIdent,
                null,
                tilgangskontrollService,
                persondataService,
                norg2Client,
                Set.of(navEnhet),
                new SlettemerkeProperties(),
                TestData.INGEN_AD_GRUPPER,
                veilarboppfolgingService,
                featureToggleServiceMock,
                eregService
        );

        when(persondataService.hentNavn(any())).thenReturn(new Navn("Donald", "", "Duck"));
        when(persondataService.hentGeografiskTilknytning(any())).thenReturn(Optional.of("0904"));
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(veileder), any())).thenReturn(true);
        when(norg2Client.hentGeografiskEnhet(any()))
                .thenReturn(new Norg2GeoResponse(
                        TestData.ENHET_GEOGRAFISK.getNavn(),
                        TestData.ENHET_GEOGRAFISK.getVerdi()
                ));
        when(eregService.hentVirksomhet(any())).thenReturn(new Organisasjon(TestData.etBedriftNr(), "Arbeidsplass AS"));

        Avtale avtale = veileder.opprettAvtale(opprettAvtale);

        assertThat(avtale.getVeilederNavIdent()).isEqualTo(TestData.enNavIdent());
        assertThat(avtale.getGjeldendeInnhold().getDeltakerFornavn()).isEqualTo("Donald");
        assertThat(avtale.getGjeldendeInnhold().getDeltakerEtternavn()).isEqualTo("Duck");
        assertThat(avtale.getEnhetGeografisk()).isEqualTo(TestData.ENHET_GEOGRAFISK.getVerdi());
    }

    @Test
    public void opprettAvtale__skal_ikke_slettemerkes() {
        NavIdent navIdent = new NavIdent("Z123456");
        NavEnhet navEnhet = TestData.ENHET_OPPFØLGING;

        OpprettAvtale opprettAvtale = new OpprettAvtale(
                TestData.etFodselsnummer(),
                TestData.etBedriftNr(),
                Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD
        );

        VeilarboppfolgingService veilarboppfolgingService = mock(VeilarboppfolgingService.class);
        Norg2Client norg2Client = mock(Norg2Client.class);
        PersondataService persondataService = mock(PersondataService.class);
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        FeatureToggleService featureToggleServiceMock = mock(FeatureToggleService.class);
        EregService eregService  = mock(EregService.class);

        Veileder veileder = new Veileder(
                navIdent,
                null,
                tilgangskontrollService,
                persondataService,
                norg2Client,
                Set.of(navEnhet),
                new SlettemerkeProperties(),
                TestData.INGEN_AD_GRUPPER,
                veilarboppfolgingService,
                featureToggleServiceMock,
                eregService
        );

        when(persondataService.hentNavn(any())).thenReturn(Navn.TOMT_NAVN);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(veileder), any())).thenReturn(true);
        when(norg2Client.hentGeografiskEnhet(any()))
                .thenReturn(
                        new Norg2GeoResponse(TestData.ENHET_GEOGRAFISK.getNavn(),
                                TestData.ENHET_GEOGRAFISK.getVerdi())
                );
        when(eregService.hentVirksomhet(any())).thenReturn(new Organisasjon(TestData.etBedriftNr(), "Arbeidsplass AS"));

        Avtale avtale = veileder.opprettAvtale(opprettAvtale);
        assertThat(avtale.isSlettemerket()).isFalse();
    }

    @Test
    public void slettemerke__avtale_med_tilgang() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        NavIdent navIdent = new NavIdent("Z123456");

        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        SlettemerkeProperties slettemerkeProperties = new SlettemerkeProperties();
        slettemerkeProperties.setIdent(List.of(navIdent));
        FeatureToggleService featureToggleServiceMock = mock(FeatureToggleService.class);
        Veileder veileder = new Veileder(
                navIdent,
                null,
                tilgangskontrollService,
                mock(PersondataService.class),
                mock(Norg2Client.class),
                Set.of(new NavEnhet("4802", "Trysil")),
                slettemerkeProperties,
                TestData.INGEN_AD_GRUPPER,
                mock(VeilarboppfolgingService.class),
                featureToggleServiceMock,
                mock(EregService.class)
        );
        when(tilgangskontrollService.hentSkrivetilgang(any(Veileder.class), any(Fnr.class))).thenReturn(new Tilgang.Tillat());
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(veileder), eq(avtale.getDeltakerFnr())))
                .thenReturn(true);

        veileder.slettemerk(avtale);
        assertThat(avtale.isSlettemerket()).isTrue();
    }

    @Test
    public void slettemerke__avtale_uten_tilgang() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();

        NavIdent navIdent = new NavIdent("X123456");

        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        FeatureToggleService featureToggleServiceMock = mock(FeatureToggleService.class);
        SlettemerkeProperties slettemerkeProperties = new SlettemerkeProperties();
        slettemerkeProperties.setIdent(List.of(new NavIdent("Z123456")));
        Veileder veileder = new Veileder(
                navIdent,
                null,
                tilgangskontrollService,
                mock(PersondataService.class),
                mock(Norg2Client.class),
                Set.of(new NavEnhet("4802", "Trysil")),
                slettemerkeProperties,
                TestData.INGEN_AD_GRUPPER,
                mock(VeilarboppfolgingService.class),
                featureToggleServiceMock,
                mock(EregService.class)
        );
        when(tilgangskontrollService.hentSkrivetilgang(eq(veileder), eq(avtale.getDeltakerFnr()))).thenReturn(new Tilgang.Tillat());
        assertThatThrownBy(() -> veileder.slettemerk(avtale)).isExactlyInstanceOf(IkkeAdminTilgangException.class);
    }

    @Test
    public void slettemerket_avtale_eksisterer_ikke() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        avtale.setSlettemerket(true);
        Veileder veileder = TestData.enVeileder(avtale);
        assertThat(veileder.avtalenEksisterer(avtale)).isFalse();
    }

    @Test
    public void feilregistrerte_avtale_eksisterer_ikke() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        avtale.setFeilregistrert(true);
        Veileder veileder = TestData.enVeileder(avtale);
        assertThat(veileder.avtalenEksisterer(avtale)).isFalse();
    }

    @Test
    public void opprettelse_av_tiltak_med_forskjellige_kvalifiseringskoder(){
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Oppfølgingsstatus oppfølgingsstatus = new Oppfølgingsstatus(
                Formidlingsgruppe.ARBEIDSSOKER,
                Kvalifiseringsgruppe.IKKE_VURDERT,
                "0906"
        );
        VeilarboppfolgingService veilarboppfolgingService = Mockito.spy(new VeilarboppfolgingService(null));
        Mockito.doReturn(oppfølgingsstatus).when(veilarboppfolgingService).hentOppfolgingsstatus(anyString());

        assertThatThrownBy(() -> veilarboppfolgingService.hentOgSjekkOppfolgingstatus(avtale))
                .isExactlyInstanceOf(FeilkodeException.class)
                .hasMessage(Feilkode.KVALIFISERINGSGRUPPE_IKKE_RETTIGHET.name());
    }

    @Test
    public void hentAlleAvtalerMedMuligTilgang_kaller_sokEtterAvtale_med_veileder_ident_dersom_sporring_ikke_har_andre_enheter(){
        AvtaleRepository avtaleRepository = spy(AvtaleRepository.class);
        Veileder veileder = TestData.enVeileder(new NavIdent("Z123456"));

        AvtaleQueryParameter query = new AvtaleQueryParameter();
        veileder.hentAlleAvtalerMedMuligTilgang(avtaleRepository, query, Pageable.unpaged());
        verify(avtaleRepository).sokEtterAvtale(new NavIdent("Z123456"), null, null, null, null, null, null, false, Pageable.unpaged());

        query.setTiltakstype(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
        query.setStatus(Status.PÅBEGYNT);
        veileder.hentAlleAvtalerMedMuligTilgang(avtaleRepository, query, Pageable.unpaged());
        verify(avtaleRepository).sokEtterAvtale(new NavIdent("Z123456"), null, null, null, null, Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, Status.PÅBEGYNT, false, Pageable.unpaged());
    }

    @Test
    public void hentAlleAvtalerMedMuligTilgang_kaller_sokEtterAvtale_med_veileder_nav_ident_dersom_den_er_i_sporringen(){
        AvtaleRepository avtaleRepository = spy(AvtaleRepository.class);
        Veileder veileder = TestData.enVeileder(new NavIdent("Z123456"));

        AvtaleQueryParameter query = new AvtaleQueryParameter();
        query.setVeilederNavIdent(new NavIdent("Z000000"));

        veileder.hentAlleAvtalerMedMuligTilgang(avtaleRepository, query, Pageable.unpaged());
        verify(avtaleRepository).sokEtterAvtale(new NavIdent("Z000000"), null, null, null, null, null, null, false, Pageable.unpaged());

        query.setTiltakstype(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
        query.setStatus(Status.PÅBEGYNT);
        veileder.hentAlleAvtalerMedMuligTilgang(avtaleRepository, query, Pageable.unpaged());
        verify(avtaleRepository).sokEtterAvtale(new NavIdent("Z000000"), null, null, null, null, Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, Status.PÅBEGYNT, false, Pageable.unpaged());
    }

    @Test
    public void hentAlleAvtalerMedMuligTilgang_kaller_sokEtterAvtale_med_ufordelt_true_dersom_avtalen_er_ufordelt(){
        AvtaleRepository avtaleRepository = spy(AvtaleRepository.class);
        Veileder veileder = TestData.enVeileder(new NavIdent("Z123456"));

        AvtaleQueryParameter query = new AvtaleQueryParameter();
        query.setErUfordelt(true);

        veileder.hentAlleAvtalerMedMuligTilgang(avtaleRepository, query, Pageable.unpaged());
        verify(avtaleRepository).sokEtterAvtale(null, null, null, null, null, null, null, true, Pageable.unpaged());

        query.setNavEnhet("4802");
        veileder.hentAlleAvtalerMedMuligTilgang(avtaleRepository, query, Pageable.unpaged());
        verify(avtaleRepository).sokEtterAvtale(null, null, null, null, "4802", null, null, true, Pageable.unpaged());

        query.setTiltakstype(Tiltakstype.SOMMERJOBB);
        query.setStatus(Status.KLAR_FOR_OPPSTART);
        query.setNavEnhet("4802");
        veileder.hentAlleAvtalerMedMuligTilgang(avtaleRepository, query, Pageable.unpaged());
        verify(avtaleRepository).sokEtterAvtale(null, null, null, null, "4802", Tiltakstype.SOMMERJOBB, Status.KLAR_FOR_OPPSTART, true, Pageable.unpaged());
    }

    @Test
    public void setter_alle_parameter_dersom_de_eksisterer(){
        AvtaleRepository avtaleRepository = spy(AvtaleRepository.class);
        Veileder veileder = TestData.enVeileder(new NavIdent("Z123456"));
        Fnr fnr = Fnr.generer(1978, 9, 10);

        AvtaleQueryParameter query = new AvtaleQueryParameter();
        query.setDeltakerFnr(fnr);
        query.setBedriftNr(new BedriftNr("123456789"));
        query.setAvtaleNr(1);
        query.setNavEnhet("4802");

        veileder.hentAlleAvtalerMedMuligTilgang(avtaleRepository, query, Pageable.unpaged());
        verify(avtaleRepository).sokEtterAvtale(null, 1, fnr, new BedriftNr("123456789"), "4802", null, null, false, Pageable.unpaged());

        query.setVeilederNavIdent(new NavIdent("Z000000"));
        veileder.hentAlleAvtalerMedMuligTilgang(avtaleRepository, query, Pageable.unpaged());
        verify(avtaleRepository).sokEtterAvtale(new NavIdent("Z000000"), 1, fnr, new BedriftNr("123456789"), "4802", null, null, false, Pageable.unpaged());

        query.setTiltakstype(Tiltakstype.ARBEIDSTRENING);
        query.setStatus(Status.MANGLER_GODKJENNING);
        veileder.hentAlleAvtalerMedMuligTilgang(avtaleRepository, query, Pageable.unpaged());
        verify(avtaleRepository).sokEtterAvtale(new NavIdent("Z000000"), 1, fnr, new BedriftNr("123456789"), "4802", Tiltakstype.ARBEIDSTRENING, Status.MANGLER_GODKJENNING, false, Pageable.unpaged());
    }

    @Test
    public void oppdaterOppfølgingOgGeoEnhetEtterForespørsel_skal_endre_oppfølgingsenhet_men_ikke_noe_annet() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setEnhetOppfolging("0101");
        avtale.setDeltakerFnr(new Fnr("31129118213"));
        avtale.setFormidlingsgruppe(Formidlingsgruppe.FRA_NAV_NO);
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS);
        Veileder veileder = TestData.enVeileder(avtale);

        assertThat(avtale.getEnhetOppfolging()).isEqualTo("0101");
        assertThat(avtale.getFormidlingsgruppe()).isEqualTo(Formidlingsgruppe.FRA_NAV_NO);
        assertThat(avtale.getKvalifiseringsgruppe()).isEqualTo(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS);
        veileder.oppdaterOppfølgingOgGeoEnhetEtterForespørsel(avtale);

        // Oppdaterer oppfølgingsenhet
        assertThat(avtale.getEnhetOppfolging()).isEqualTo("0906");
        // Og ingen andre oppfølgingstatus reltaterte endringer:
        assertThat(avtale.getFormidlingsgruppe()).isEqualTo(Formidlingsgruppe.FRA_NAV_NO);
        assertThat(avtale.getKvalifiseringsgruppe()).isEqualTo(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS);

    }
}
