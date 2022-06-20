package no.nav.tag.tiltaksgjennomforing.avtale;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.SlettemerkeProperties;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.enhet.Formidlingsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2GeoResponse;
import no.nav.tag.tiltaksgjennomforing.enhet.Oppfølgingsstatus;
import no.nav.tag.tiltaksgjennomforing.enhet.VeilarbArenaClient;
import no.nav.tag.tiltaksgjennomforing.exceptions.ErAlleredeVeilederException;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.IkkeAdminTilgangException;
import no.nav.tag.tiltaksgjennomforing.exceptions.KanIkkeGodkjenneAvtalePåKode6Exception;
import no.nav.tag.tiltaksgjennomforing.exceptions.VeilederSkalGodkjenneSistException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;
import no.nav.tag.tiltaksgjennomforing.persondata.PdlRespons;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class VeilederTest {
    @Test
    public void godkjennAvtale__kan_ikke_godkjenne_foerst() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Veileder veileder = TestData.enVeileder(avtale);
        assertThatThrownBy(() -> veileder.godkjennAvtale(avtale.getSistEndret(), avtale)).isExactlyInstanceOf(VeilederSkalGodkjenneSistException.class);
    }

    @Test
    public void godkjennAvtale__kan_godkjenne_sist() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.godkjennAvtale(avtale.getSistEndret(), avtale);
        assertThat(avtale.erGodkjentAvVeileder()).isTrue();
    }

    @Test
    public void godkjennAvtale__kan_ikke_godkjenne_kode6() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        PersondataService persondataService = mock(PersondataService.class);
        when(persondataService.erKode6(avtale.getDeltakerFnr())).thenReturn(true);
        Veileder veileder = TestData.enVeileder(avtale, persondataService);
        assertThatThrownBy(() -> veileder.godkjennAvtale(avtale.getSistEndret(), avtale)).isExactlyInstanceOf(KanIkkeGodkjenneAvtalePåKode6Exception.class);
    }

    @Test
    public void godkjennForVeilederOgDeltaker__kan_ikke_godkjenne_kode6() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        PersondataService persondataService = mock(PersondataService.class);
        when(persondataService.erKode6(avtale.getDeltakerFnr())).thenReturn(true);
        Veileder veileder = TestData.enVeileder(avtale, persondataService);
        assertThatThrownBy(() -> veileder.godkjennForVeilederOgDeltaker(TestData.enGodkjentPaVegneGrunn(), avtale)).isExactlyInstanceOf(KanIkkeGodkjenneAvtalePåKode6Exception.class);
    }

    @Test
    public void opphevGodkjenninger__kan_ikke_oppheve_godkjenninger_når_avtale_er_inngått() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setAvtaleInngått(LocalDateTime.now());
        Veileder veileder = TestData.enVeileder(avtale);
        assertFeilkode(Feilkode.KAN_IKKE_OPPHEVE_GODKJENNINGER_VED_INNGAATT_AVTALE, () -> veileder.opphevGodkjenninger(avtale));
    }

    @Test
    public void opphevGodkjenninger__kan_oppheve_godkjenninger_hvis_alle_parter_har_godkjent_men_ikke_inngått() {
        Now.fixedDate(LocalDate.of(2021, 6, 1));
        Avtale avtale = TestData.enSommerjobbAvtaleGodkjentAvVeileder();
        Veileder veileder = TestData.enVeileder(avtale);
        assertThat(avtale.godkjentAvArbeidsgiver() != null && avtale.godkjentAvDeltaker() != null && avtale.godkjentAvVeileder() != null).isTrue();
        assertThat(avtale.erAvtaleInngått()).isFalse();
        veileder.opphevGodkjenninger(avtale);
        assertThat(avtale.godkjentAvArbeidsgiver() == null && avtale.godkjentAvDeltaker() == null && avtale.godkjentAvVeileder() == null).isTrue();
        Now.resetClock();
    }

    @Test
    public void opphevGodkjenninger__kan_ikke_oppheve_arbeidstrening_hvis_alle_parter_har_godkjent() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        Veileder veileder = TestData.enVeileder(avtale);
        avtale.endreAvtale(Instant.now(), TestData.endringPåAlleArbeidstreningFelter(), Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()), List.of());
        arbeidsgiver.godkjennAvtale(Instant.now(), avtale);
        veileder.godkjennForVeilederOgDeltaker(TestData.enGodkjentPaVegneGrunn(), avtale);

        assertFeilkode(Feilkode.KAN_IKKE_OPPHEVE_GODKJENNINGER_VED_INNGAATT_AVTALE, () -> veileder.opphevGodkjenninger(avtale));
    }

    @Test
    public void opphevgodkjenninger__kan_ikke_oppheve_hvis_første_tilskuddsperiode_er_godkjent() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);

        // Gi veileder tilgang til deltaker
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(avtale.getVeilederNavIdent()), any(Fnr.class))).thenReturn(true);

        // Avtalens bedriftnummer i miljøvariabel med pilotbedrifter
        TilskuddsperiodeConfig tilskuddsperiodeConfig = new TilskuddsperiodeConfig();
        tilskuddsperiodeConfig.setPilotvirksomheter(List.of(avtale.getBedriftNr()));
        // Veileder med injecta pilotbedrift
        Veileder veileder = new Veileder(avtale.getVeilederNavIdent(),
                tilgangskontrollService, mock(PersondataService.class), mock(Norg2Client.class),
                Set.of(new NavEnhet("4802", "Trysil")), mock(SlettemerkeProperties.class),
                tilskuddsperiodeConfig, false, mock(VeilarbArenaClient.class));

        avtale.endreAvtale(Instant.now(), TestData.endringPåAlleFelter(), Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()), List.of());
        arbeidsgiver.godkjennAvtale(Instant.now(), avtale);
        veileder.godkjennForVeilederOgDeltaker(TestData.enGodkjentPaVegneGrunn(), avtale);

        assertThat(avtale.erAvtaleInngått()).isFalse();

        veileder.opphevGodkjenninger(avtale);
        arbeidsgiver.godkjennAvtale(Instant.now(), avtale);
        veileder.godkjennForVeilederOgDeltaker(TestData.enGodkjentPaVegneGrunn(), avtale);

        Beslutter beslutter = TestData.enBeslutter(avtale);
        beslutter.godkjennTilskuddsperiode(avtale, "0000");

        assertThat(avtale.erAvtaleInngått()).isTrue();
        assertFeilkode(Feilkode.KAN_IKKE_OPPHEVE_GODKJENNINGER_VED_INNGAATT_AVTALE, () -> veileder.opphevGodkjenninger(avtale));
    }


    @Test
    public void annullerAvtale__kan_annuller_avtale_etter_veiledergodkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.annullerAvtale(avtale.getSistEndret(), "enGrunn", avtale);
        assertThat(avtale.getAnnullertTidspunkt()).isNotNull();
        assertThat(avtale.getAnnullertGrunn()).isEqualTo("enGrunn");
    }

    @Test
    public void annullerAvtale__kan_annullere_avtale_foer_veiledergodkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.annullerAvtale(avtale.getSistEndret(), "enGrunn", avtale);
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
    public void overtarAvtale__feil_hvis_samme_ident() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Veileder veileder = TestData.enVeileder(avtale);
        assertThatThrownBy(() -> veileder.overtaAvtale(avtale)).isExactlyInstanceOf(ErAlleredeVeilederException.class);
    }

    @Test
    public void oprettAvtale__setter_startverdier_på_avtale() {
        OpprettAvtale opprettAvtale = new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        PersondataService persondataService = mock(PersondataService.class);
        Norg2Client norg2Client = mock(Norg2Client.class);
        final PdlRespons pdlRespons = TestData.enPdlrespons(false);
        VeilarbArenaClient veilarbArenaClient = mock(VeilarbArenaClient.class);

        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(TestData.enNavIdent()), eq(TestData.etFodselsnummer()))).thenReturn(true);
        when(persondataService.hentPersondata(TestData.etFodselsnummer())).thenReturn(pdlRespons);
        when(persondataService.erKode6(pdlRespons)).thenCallRealMethod();
        when(norg2Client.hentGeografiskEnhet(pdlRespons.getData().getHentGeografiskTilknytning().getGtBydel())).thenReturn(new Norg2GeoResponse(TestData.ENHET_GEOGRAFISK.getNavn(), TestData.ENHET_GEOGRAFISK.getVerdi()));

        Veileder veileder = new Veileder(TestData.enNavIdent(), tilgangskontrollService, persondataService, norg2Client,
                Set.of(TestData.ENHET_GEOGRAFISK), new SlettemerkeProperties(), new TilskuddsperiodeConfig(), false, veilarbArenaClient);
        Avtale avtale = veileder.opprettAvtale(opprettAvtale);

        assertThat(avtale.getVeilederNavIdent()).isEqualTo(TestData.enNavIdent());
        assertThat(avtale.getGjeldendeInnhold().getDeltakerFornavn()).isEqualTo("Donald");
        assertThat(avtale.getGjeldendeInnhold().getDeltakerEtternavn()).isEqualTo("Duck");
        assertThat(avtale.getEnhetGeografisk()).isEqualTo(TestData.ENHET_GEOGRAFISK.getVerdi());
    }

    @Test
    public void opprettAvtale__skal_ikke_slettemerkes() {
        OpprettAvtale opprettAvtale = new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
        Veileder veileder = TestData.enVeileder(new NavIdent("Z123456"));
        Avtale avtale = veileder.opprettAvtale(opprettAvtale);
        assertThat(avtale.isSlettemerket()).isFalse();
    }

    @Test
    public void slettemerke__avtale_med_tilgang() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        NavIdent navIdent = new NavIdent("Z123456");

        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(navIdent), eq(avtale.getDeltakerFnr()))).thenReturn(true);

        SlettemerkeProperties slettemerkeProperties = new SlettemerkeProperties();
        slettemerkeProperties.setIdent(List.of(navIdent));
        Veileder veileder = new Veileder(navIdent, tilgangskontrollService, mock(PersondataService.class), mock(Norg2Client.class), Set.of(new NavEnhet("4802", "Trysil")), slettemerkeProperties, new TilskuddsperiodeConfig(), false, mock(VeilarbArenaClient.class));
        veileder.slettemerk(avtale);
        assertThat(avtale.isSlettemerket()).isTrue();
    }

    @Test
    public void slettemerke__avtale_uten_tilgang() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();

        NavIdent navIdent = new NavIdent("X123456");

        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(navIdent), eq(avtale.getDeltakerFnr()))).thenReturn(true);

        SlettemerkeProperties slettemerkeProperties = new SlettemerkeProperties();
        slettemerkeProperties.setIdent(List.of(new NavIdent("Z123456")));
        Veileder veileder = new Veileder(navIdent, tilgangskontrollService, mock(PersondataService.class), mock(Norg2Client.class), Set.of(new NavEnhet("4802", "Trysil")), slettemerkeProperties, new TilskuddsperiodeConfig(), false, mock(VeilarbArenaClient.class));
        assertThatThrownBy(() -> veileder.slettemerk(avtale)).isExactlyInstanceOf(IkkeAdminTilgangException.class);
    }

    @Test
    public void slettemerket_ikke_tilgang_til_avtale() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        avtale.setSlettemerket(true);
        Veileder veileder = TestData.enVeileder(avtale);
        assertThat(veileder.harTilgang(avtale)).isFalse();
    }

    @Test
    public void opprettelse_av_tiltak_med_forskjellige_kvalifiseringskoder(){
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Oppfølgingsstatus oppfølgingsstatus = new Oppfølgingsstatus(Formidlingsgruppe.ARBEIDSSOKER, Kvalifiseringsgruppe.IKKE_VURDERT, "0906");
        VeilarbArenaClient veilarbArenaClient = Mockito.spy(new VeilarbArenaClient(null, null));
        Mockito.doReturn(oppfølgingsstatus).when(veilarbArenaClient).hentOppfølgingStatus(Mockito.anyString());

        assertThatThrownBy(() -> veilarbArenaClient.sjekkOppfølingStatus(avtale)).isExactlyInstanceOf(FeilkodeException.class).hasMessage(Feilkode.KVALIFISERINGSGRUPPE_IKKE_RETTIGHET.name());
    }
}
