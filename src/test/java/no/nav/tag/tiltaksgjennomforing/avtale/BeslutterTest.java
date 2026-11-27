package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.SyntetiskFnr;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Tilgang;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.brev.PostutsendelseService;
import no.nav.tag.tiltaksgjennomforing.enhet.Innsatsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2EnhetStatus;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2OppfølgingResponse;
import no.nav.tag.tiltaksgjennomforing.enhet.Oppfølgingsstatus;
import no.nav.tag.tiltaksgjennomforing.enhet.veilarb.VeilarbService;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;
import no.nav.tag.tiltaksgjennomforing.orgenhet.EregService;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import no.nav.team_tiltak.felles.persondata.pdl.domene.Diskresjonskode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SyntetiskFnr
class BeslutterTest {

    @AfterEach
    void tearDown() {
        Now.resetClock();
    }

    @Test
    public void toggle_godkjent_for_etterregistrering() {

        //GITT
        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD), Avtaleopphav.VEILEDER, TestData.enNavIdent());
        Beslutter beslutter = TestData.enBeslutter(avtale);

        // NÅR
        beslutter.setOmAvtalenKanEtterregistreres(avtale);
        assertThat(avtale.isGodkjentForEtterregistrering()).isTrue();

        beslutter.setOmAvtalenKanEtterregistreres(avtale);
        assertThat(avtale.isGodkjentForEtterregistrering()).isFalse();
    }

    @Test
    public void kan_ikke_godkjenne_periode_på_enhet_som_ikke_finnes() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);

        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        PersondataService persondataService = mock(PersondataService.class);
        FeatureToggleService featureToggleServiceMock = mock(FeatureToggleService.class);
        VeilarbService veilarboppfolgingService = mock(VeilarbService.class);

        Veileder veileder = new Veileder(
                avtale.getVeilederNavIdent(),
                null,
                tilgangskontrollService,
                persondataService,
                mock(Norg2Client.class),
                Set.of(new NavEnhet("4802", "Trysil")),
                TestData.INGEN_AD_GRUPPER,
                veilarboppfolgingService,
                featureToggleServiceMock,
                mock(EregService.class),
                mock(PostutsendelseService.class)
        );

        when(tilgangskontrollService.hentSkrivetilgang(eq(veileder), any(Fnr.class))).thenReturn(new Tilgang.Tillat());
        when(persondataService.hentDiskresjonskode(any(Fnr.class))).thenReturn(Diskresjonskode.UGRADERT);
        when(veilarboppfolgingService.hentOgSjekkOppfolgingstatus(any(Avtale.class))).thenReturn(new Oppfølgingsstatus(
            "0906",
            "Nav Oslo",
            Innsatsgruppe.TRENGER_VEILEDNING
        ));

        avtale.endreAvtale(
                TestData.endringPåAlleLønnstilskuddFelter(),
                Avtalerolle.VEILEDER
        );
        arbeidsgiver.godkjennAvtale(avtale);
        veileder.godkjennForVeilederOgDeltaker(TestData.enGodkjentPaVegneGrunn(), avtale);
        assertThat(avtale.erAvtaleInngått()).isFalse();
        Beslutter beslutter = TestData.enBeslutter(avtale);
        assertFeilkode(
                Feilkode.ENHET_FINNES_IKKE,
                () -> beslutter.godkjennTilskuddsperiode(avtale, "9999")
        );
        assertThat(avtale.erAvtaleInngått()).isFalse();
    }

    @Test
    public void godkjenner_tilskuddsperiode_i_fortiden_nar_feilkode_er_bekreftet() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleGodkjentAvVeileder();
        TilskuddPeriode periode = avtale.getGjeldendeTilskuddsperiode();
        Now.fixedDate(periode.getSluttDato().plusDays(1));

        VeilarbService veilarbService = mock(VeilarbService.class);
        when(veilarbService.hentOgSjekkOppfolgingstatus(avtale))
            .thenThrow(new FeilkodeException(Feilkode.FANT_IKKE_INNSATSBEHOV));
        Beslutter beslutter = enBeslutterMedMocks(avtale, veilarbService, true);

        assertFeilkode(
            Feilkode.FANT_IKKE_INNSATSBEHOV,
            () -> beslutter.godkjennTilskuddsperiode(avtale, "0906", null)
        );
        assertThat(periode.getStatus()).isEqualTo(TilskuddPeriodeStatus.UBEHANDLET);

        beslutter.godkjennTilskuddsperiode(avtale, "0906", Feilkode.FANT_IKKE_INNSATSBEHOV);
        assertThat(periode.getStatus()).isEqualTo(TilskuddPeriodeStatus.GODKJENT);
    }

    @Test
    public void godkjenner_tilskuddsperiode_i_fortiden_nar_innsatsgruppe_mangler_er_bekreftet() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleGodkjentAvVeileder();
        avtale.setInnsatsgruppe(null);
        TilskuddPeriode periode = avtale.getGjeldendeTilskuddsperiode();
        Now.fixedDate(periode.getSluttDato().plusDays(1));

        VeilarbService veilarbService = mock(VeilarbService.class);
        when(veilarbService.hentOgSjekkOppfolgingstatus(avtale))
            .thenReturn(new Oppfølgingsstatus("0906", "Nav Oslo", Innsatsgruppe.TRENGER_VEILEDNING));
        Beslutter beslutter = enBeslutterMedMocks(avtale, veilarbService, true);

        assertFeilkode(
            Feilkode.INNSATSGRUPPE_MANGLER,
            () -> beslutter.godkjennTilskuddsperiode(avtale, "0906", null)
        );

        beslutter.godkjennTilskuddsperiode(avtale, "0906", Feilkode.INNSATSGRUPPE_MANGLER);
        assertThat(periode.getStatus()).isEqualTo(TilskuddPeriodeStatus.GODKJENT);
    }

    @Test
    public void kan_ikke_bekrefte_feilkode_for_tilskuddsperiode_som_ikke_er_i_fortiden() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleGodkjentAvVeileder();
        TilskuddPeriode periode = avtale.getGjeldendeTilskuddsperiode();
        Now.fixedDate(periode.getSluttDato());

        VeilarbService veilarbService = mock(VeilarbService.class);
        when(veilarbService.hentOgSjekkOppfolgingstatus(avtale))
            .thenThrow(new FeilkodeException(Feilkode.FANT_IKKE_INNSATSBEHOV));
        Beslutter beslutter = enBeslutterMedMocks(avtale, veilarbService, true);

        assertFeilkode(
            Feilkode.FANT_IKKE_INNSATSBEHOV,
            () -> beslutter.godkjennTilskuddsperiode(avtale, "0906", Feilkode.FANT_IKKE_INNSATSBEHOV)
        );
        assertThat(periode.getStatus()).isEqualTo(TilskuddPeriodeStatus.UBEHANDLET);
    }

    @Test
    public void kan_ikke_bekrefte_feil_feilkode_eller_feilkode_utenfor_tillatelseslisten() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleGodkjentAvVeileder();
        TilskuddPeriode periode = avtale.getGjeldendeTilskuddsperiode();
        Now.fixedDate(periode.getSluttDato().plusDays(1));

        VeilarbService veilarbService = mock(VeilarbService.class);
        Beslutter beslutter = enBeslutterMedMocks(avtale, veilarbService, true);

        doThrow(new FeilkodeException(Feilkode.FANT_IKKE_INNSATSBEHOV))
            .when(veilarbService).hentOgSjekkOppfolgingstatus(avtale);
        assertFeilkode(
            Feilkode.FANT_IKKE_INNSATSBEHOV,
            () -> beslutter.godkjennTilskuddsperiode(avtale, "0906", Feilkode.INNSATSGRUPPE_MANGLER)
        );

        doThrow(new FeilkodeException(Feilkode.ENHET_MANGLER))
            .when(veilarbService).hentOgSjekkOppfolgingstatus(avtale);
        assertFeilkode(
            Feilkode.ENHET_MANGLER,
            () -> beslutter.godkjennTilskuddsperiode(avtale, "0906", Feilkode.ENHET_MANGLER)
        );

        doThrow(new FeilkodeException(Feilkode.HENTING_AV_INNSATSBEHOV_FEILET))
            .when(veilarbService).hentOgSjekkOppfolgingstatus(avtale);
        assertFeilkode(
            Feilkode.HENTING_AV_INNSATSBEHOV_FEILET,
            () -> beslutter.godkjennTilskuddsperiode(avtale, "0906", Feilkode.HENTING_AV_INNSATSBEHOV_FEILET)
        );

        assertThat(periode.getStatus()).isEqualTo(TilskuddPeriodeStatus.UBEHANDLET);
    }

    @Test
    public void sjekker_fortsatt_enhetstilgang_for_tiltak_nar_feilkode_er_bekreftet() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleGodkjentAvVeileder();
        TilskuddPeriode periode = avtale.getGjeldendeTilskuddsperiode();
        Now.fixedDate(periode.getSluttDato().plusDays(1));

        VeilarbService veilarbService = mock(VeilarbService.class);
        when(veilarbService.hentOgSjekkOppfolgingstatus(avtale))
            .thenThrow(new FeilkodeException(Feilkode.FANT_IKKE_INNSATSBEHOV));
        Beslutter beslutter = enBeslutterMedMocks(avtale, veilarbService, false);

        assertFeilkode(
            Feilkode.ENHET_IKKE_TILGANG_PA_TILTAK,
            () -> beslutter.godkjennTilskuddsperiode(avtale, "0906", Feilkode.FANT_IKKE_INNSATSBEHOV)
        );
        assertThat(periode.getStatus()).isEqualTo(TilskuddPeriodeStatus.UBEHANDLET);
    }

    @Test
    public void kaster_exception_nar_avtale_mangler_gjeldende_tilskuddsperiode() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddsjobbAvtale();
        Beslutter beslutter = enBeslutterMedMocks(avtale, mock(VeilarbService.class), true);

        assertThatThrownBy(() -> beslutter.godkjennTilskuddsperiode(avtale, "0906", null))
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    private static Beslutter enBeslutterMedMocks(Avtale avtale, VeilarbService veilarbService, boolean harEnhetTilgang) {
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        Norg2Client norg2Client = mock(Norg2Client.class);
        FeatureToggleService featureToggleService = mock(FeatureToggleService.class);

        Beslutter beslutter = new Beslutter(
            TestData.enNavIdent2(),
            null,
            Set.of(),
            tilgangskontrollService,
            norg2Client,
            mock(PersondataService.class),
            TestData.INGEN_AD_GRUPPER,
            veilarbService,
            featureToggleService,
            mock(EregService.class)
        );

        when(tilgangskontrollService.hentSkrivetilgang(eq(beslutter), eq(avtale.getDeltakerFnr())))
            .thenReturn(new Tilgang.Tillat());
        when(norg2Client.hentOppfølgingsEnhet("0906"))
            .thenReturn(new Norg2OppfølgingResponse(906, "0906", "Oslo", Norg2EnhetStatus.AKTIV));
        when(featureToggleService.harEnhetTilgangPaTiltak(eq(avtale.getTiltakstype()), any()))
            .thenReturn(harEnhetTilgang);

        return beslutter;
    }
}
