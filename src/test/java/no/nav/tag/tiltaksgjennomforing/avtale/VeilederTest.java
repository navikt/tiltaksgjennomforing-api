package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.SlettemerkeProperties;
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
import no.nav.tag.tiltaksgjennomforing.exceptions.KanIkkeGodkjenneAvtalePåKode6Exception;
import no.nav.tag.tiltaksgjennomforing.exceptions.VeilederSkalGodkjenneSistException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;
import no.nav.tag.tiltaksgjennomforing.persondata.PdlRespons;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;
import static no.nav.tag.tiltaksgjennomforing.avtale.TestData.avtalerMedTilskuddsperioder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class VeilederTest {
    @Test
    public void godkjennAvtale__kan_ikke_godkjenne_foerst() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Veileder veileder = TestData.enVeileder(avtale);
        assertThatThrownBy(() -> veileder.godkjennAvtale(avtale.getSistEndret(), avtale))
                .isExactlyInstanceOf(VeilederSkalGodkjenneSistException.class);
    }

    @Test
    public void godkjennAvtale__kan_godkjenne_sist() {
        // GITT
        Avtale avtale = TestData.enVarigLonnstilskuddAvtaleMedAltUtfylt();
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS);
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        VeilarboppfolgingService veilarboppfolgingService = mock(VeilarboppfolgingService.class);
        // NÅR
        when(veilarboppfolgingService.hentOgSjekkOppfolgingstatus(avtale)).thenReturn(new Oppfølgingsstatus(Formidlingsgruppe.ARBEIDSSOKER, Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS, "0906"));
        Veileder veileder = TestData.enVeileder(TestData.enNavIdent(),veilarboppfolgingService);
        veileder.godkjennAvtale(avtale.getSistEndret(), avtale);

        // SÅ
        assertThat(avtale.erGodkjentAvVeileder()).isTrue();
        assertThat(avtale.getKvalifiseringsgruppe().getKvalifiseringskode()).isEqualTo(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS.getKvalifiseringskode());
    }

    @Test
    public void godkjennForVeilederOgDeltaker__kan_godkjenne_med_riktig_oppfølgingsstatus() {
        // GITT
        Avtale avtale = TestData.enVarigLonnstilskuddAvtaleMedAltUtfylt();
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS);
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        VeilarboppfolgingService veilarboppfolgingService = mock(VeilarboppfolgingService.class);
        // NÅR
        when(veilarboppfolgingService.hentOgSjekkOppfolgingstatus(avtale)).thenReturn(new Oppfølgingsstatus(Formidlingsgruppe.ARBEIDSSOKER, Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS, "0906"));
        Veileder veileder = TestData.enVeileder(TestData.enNavIdent(),veilarboppfolgingService);
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
        // NÅR
        when(veilarboppfolgingService.hentOgSjekkOppfolgingstatus(avtale)).thenReturn(new Oppfølgingsstatus(Formidlingsgruppe.ARBEIDSSOKER, Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS, "0906"));
        Veileder veileder = TestData.enVeileder(TestData.enNavIdent(),veilarboppfolgingService);
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
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        VeilarboppfolgingService veilarboppfolgingService = mock(VeilarboppfolgingService.class);
        // NÅR
        when(veilarboppfolgingService.hentOgSjekkOppfolgingstatus(avtale)).thenReturn(new Oppfølgingsstatus(Formidlingsgruppe.ARBEIDSSOKER, Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS, "0906"));
        Veileder veileder = TestData.enVeileder(TestData.enNavIdent(),veilarboppfolgingService);
        GodkjentPaVegneAvArbeidsgiverGrunn godkjentPaVegneGrunn = TestData.enGodkjentPaVegneAvArbeidsgiverGrunn();
        veileder.godkjennForVeilederOgArbeidsgiver(godkjentPaVegneGrunn, avtale);

        // SÅ
        assertThat(avtale.erGodkjentAvVeileder()).isTrue();
        assertThat(avtale.getKvalifiseringsgruppe().getKvalifiseringskode()).isEqualTo(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS.getKvalifiseringskode());
    }

    @Test
    public void godkjennAvtale__kan_ikke_godkjenne_kode6() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        PersondataService persondataService = mock(PersondataService.class);
        when(persondataService.erKode6(avtale.getDeltakerFnr())).thenReturn(true);
        Veileder veileder = TestData.enVeileder(avtale, persondataService);
        assertThatThrownBy(() -> veileder.godkjennAvtale(avtale.getSistEndret(), avtale))
                .isExactlyInstanceOf(KanIkkeGodkjenneAvtalePåKode6Exception.class);
    }

    @Test
    public void godkjennForVeilederOgDeltaker__kan_ikke_godkjenne_kode6() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        PersondataService persondataService = mock(PersondataService.class);
        when(persondataService.erKode6(avtale.getDeltakerFnr())).thenReturn(true);
        Veileder veileder = TestData.enVeileder(avtale, persondataService);
        assertThatThrownBy(() -> veileder.godkjennForVeilederOgDeltaker(TestData.enGodkjentPaVegneGrunn(), avtale))
                .isExactlyInstanceOf(KanIkkeGodkjenneAvtalePåKode6Exception.class);
    }

    @Test
    public void opphevGodkjenninger__kan_ikke_oppheve_godkjenninger_når_avtale_er_inngått() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setAvtaleInngått(Now.localDateTime());
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
        Veileder veileder = TestData.enVeileder(avtale);
        avtale.endreAvtale(
                Now.instant(),
                TestData.endringPåAlleArbeidstreningFelter(),
                Avtalerolle.VEILEDER,
                avtalerMedTilskuddsperioder
        );
        arbeidsgiver.godkjennAvtale(Now.instant(), avtale);
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
        when(veilarboppfolgingServiceMock.hentOgSjekkOppfolgingstatus(avtale)).thenReturn(new Oppfølgingsstatus(Formidlingsgruppe.ARBEIDSSOKER, Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS, "0906"));
        Veileder veileder = new Veileder(
                avtale.getVeilederNavIdent(),
                tilgangskontrollService,
                mock(PersondataService.class),
                mock(Norg2Client.class),
                Set.of(new NavEnhet("4802", "Trysil")),
                mock(SlettemerkeProperties.class),

                false,
                veilarboppfolgingServiceMock);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(veileder), any(Fnr.class)))
                .thenReturn(true);

        avtale.endreAvtale(
                Now.instant(),
                TestData.endringPåAlleLønnstilskuddFelter(),
                Avtalerolle.VEILEDER,
                avtalerMedTilskuddsperioder
        );
        arbeidsgiver.godkjennAvtale(Now.instant(), avtale);
        veileder.godkjennForVeilederOgDeltaker(TestData.enGodkjentPaVegneGrunn(), avtale);

        assertThat(avtale.erAvtaleInngått()).isFalse();

        veileder.opphevGodkjenninger(avtale);
        arbeidsgiver.godkjennAvtale(Now.instant(), avtale);
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
    public void overta_avtale_hvor_veileder_allerede_er_satt_og_skal_bare_overskrive_oppfølgningsstatus_når_avtalen_endres() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();

        VeilarboppfolgingService veilarboppfolgingService = Mockito.spy(new VeilarboppfolgingService(null));
        Veileder nyVeileder = TestData.enVeileder(new NavIdent("J987654"),veilarboppfolgingService);

        Oppfølgingsstatus nyOppfølgingsstatusSomSkalIkkeSettes = new Oppfølgingsstatus(
                Formidlingsgruppe.ARBEIDSSOKER,
                Kvalifiseringsgruppe.SPESIELT_TILPASSET_INNSATS,
                "0906"
        );
        Mockito.doReturn(nyOppfølgingsstatusSomSkalIkkeSettes).when(veilarboppfolgingService).hentOppfolgingsstatus(Mockito.anyString());

        nyVeileder.hentOppfølgingFraArena(avtale,veilarboppfolgingService );

        assertThat(avtale.getKvalifiseringsgruppe()).isEqualTo(avtale.getKvalifiseringsgruppe());

        //SKal kunne endre oppfølgningsstatus på endre avtale
        Oppfølgingsstatus nyOppfølgingsstatusSomSkalSettes = new Oppfølgingsstatus(
                Formidlingsgruppe.ARBEIDSSOKER,
                Kvalifiseringsgruppe.SPESIELT_TILPASSET_INNSATS,
                "0906"
        );
        Mockito.doReturn(nyOppfølgingsstatusSomSkalSettes).when(veilarboppfolgingService).hentOppfolgingsstatus(Mockito.anyString());
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
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        endreAvtale.setLonnstilskuddProsent(null);
        avtale.getGjeldendeInnhold().setSumLonnstilskudd(null);
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        arbeidsgiver.endreAvtale(Now.instant(), endreAvtale, avtale, EnumSet.of(avtale.getTiltakstype()));
        Veileder nyVeileder = TestData.enVeileder(new NavIdent("J987654"));
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
        Veileder veileder = TestData.enVeileder(avtale);
        assertThatThrownBy(() -> veileder.overtaAvtale(avtale)).isExactlyInstanceOf(ErAlleredeVeilederException.class);
    }

    @Test
    public void overtaAvtale__skal_genere_tilskuddsperioder_hvis_ufordelt() {
        Avtale avtale = TestData.enAvtaleOpprettetAvArbeidsgiver(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
        Arbeidsgiver arbeidsgiver = TestData.enArbeidsgiver(avtale);
        arbeidsgiver.endreAvtale(
                Now.instant(),
                TestData.endringPåAlleLønnstilskuddFelter(),
                avtale,
                EnumSet.of(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD)
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
        final Fnr fnr = TestData.etFodselsnummer();
        final NavIdent navIdent = new NavIdent("Q987654");
        final NavEnhet navEnhet = TestData.ENHET_GEOGRAFISK;
        OpprettAvtale opprettAvtale = new OpprettAvtale(
                TestData.etFodselsnummer(),
                TestData.etBedriftNr(),
                Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD
        );

        final TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        final PersondataService persondataService = mock(PersondataService.class);
        final Norg2Client norg2Client = mock(Norg2Client.class);
        final PdlRespons pdlRespons = TestData.enPdlrespons(false);
        final VeilarboppfolgingService veilarboppfolgingService = mock(VeilarboppfolgingService.class);

        Veileder veileder = new Veileder(
                navIdent,
                tilgangskontrollService,
                persondataService,
                norg2Client,
                Set.of(navEnhet),
                new SlettemerkeProperties(),
                false,
                veilarboppfolgingService
        );

        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(veileder), any())).thenReturn(true);
        when(persondataService.hentPersondata(fnr)).thenReturn(pdlRespons);
        when(persondataService.erKode6(pdlRespons)).thenCallRealMethod();
        when(norg2Client.hentGeografiskEnhet(pdlRespons.getData().getHentGeografiskTilknytning().getGtBydel()))
                .thenReturn(new Norg2GeoResponse(
                        TestData.ENHET_GEOGRAFISK.getNavn(),
                        TestData.ENHET_GEOGRAFISK.getVerdi()
                ));
        when(norg2Client.hentGeografiskEnhet(pdlRespons.getData().getHentGeografiskTilknytning().getGtBydel()))
                .thenReturn(new Norg2GeoResponse(
                        TestData.ENHET_GEOGRAFISK.getNavn(),
                        TestData.ENHET_GEOGRAFISK.getVerdi()
                ));

        Avtale avtale = veileder.opprettAvtale(opprettAvtale);

        assertThat(avtale.getVeilederNavIdent()).isEqualTo(TestData.enNavIdent());
        assertThat(avtale.getGjeldendeInnhold().getDeltakerFornavn()).isEqualTo("Donald");
        assertThat(avtale.getGjeldendeInnhold().getDeltakerEtternavn()).isEqualTo("Duck");
        assertThat(avtale.getEnhetGeografisk()).isEqualTo(TestData.ENHET_GEOGRAFISK.getVerdi());
    }

    @Test
    public void opprettAvtale__skal_ikke_slettemerkes() {
        final Fnr fnr = TestData.etFodselsnummer();
        final NavIdent navIdent = new NavIdent("Z123456");
        final PdlRespons pdlRespons = TestData.enPdlrespons(false);
        final NavEnhet navEnhet = TestData.ENHET_OPPFØLGING;

        OpprettAvtale opprettAvtale = new OpprettAvtale(
                TestData.etFodselsnummer(),
                TestData.etBedriftNr(),
                Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD
        );

        final VeilarboppfolgingService veilarboppfolgingService = mock(VeilarboppfolgingService.class);
        final Norg2Client norg2Client = mock(Norg2Client.class);
        final PersondataService persondataService = mock(PersondataService.class);
        final TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);

        Veileder veileder = new Veileder(
                navIdent,
                tilgangskontrollService,
                persondataService,
                norg2Client,
                Set.of(navEnhet),
                new SlettemerkeProperties(),
                false,
                veilarboppfolgingService
        );

        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(veileder), any())).thenReturn(true);
        when(persondataService.hentPersondata(fnr)).thenReturn(pdlRespons);
        when(norg2Client.hentGeografiskEnhet(pdlRespons.getData().getHentGeografiskTilknytning().getGtBydel()))
                .thenReturn(
                        new Norg2GeoResponse(TestData.ENHET_GEOGRAFISK.getNavn(),
                                TestData.ENHET_GEOGRAFISK.getVerdi())
                );



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
        Veileder veileder = new Veileder(
                navIdent,
                tilgangskontrollService,
                mock(PersondataService.class),
                mock(Norg2Client.class),
                Set.of(new NavEnhet("4802", "Trysil")),
                slettemerkeProperties,
                false,
                mock(VeilarboppfolgingService.class)
        );

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

        SlettemerkeProperties slettemerkeProperties = new SlettemerkeProperties();
        slettemerkeProperties.setIdent(List.of(new NavIdent("Z123456")));
        Veileder veileder = new Veileder(
                navIdent,
                tilgangskontrollService,
                mock(PersondataService.class),
                mock(Norg2Client.class),
                Set.of(new NavEnhet("4802", "Trysil")),
                slettemerkeProperties,
                false,
                mock(VeilarboppfolgingService.class)
        );
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(veileder), eq(avtale.getDeltakerFnr()))).thenReturn(true);
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
        Mockito.doReturn(oppfølgingsstatus).when(veilarboppfolgingService).hentOppfolgingsstatus(Mockito.anyString());

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

        AvtaleQueryParameter query = new AvtaleQueryParameter();
        query.setDeltakerFnr(new Fnr("12345678901"));
        query.setBedriftNr(new BedriftNr("123456789"));
        query.setAvtaleNr(1);
        query.setNavEnhet("4802");

        veileder.hentAlleAvtalerMedMuligTilgang(avtaleRepository, query, Pageable.unpaged());
        verify(avtaleRepository).sokEtterAvtale(null, 1, new Fnr("12345678901"), new BedriftNr("123456789"), "4802", null, null, false, Pageable.unpaged());

        query.setVeilederNavIdent(new NavIdent("Z000000"));
        veileder.hentAlleAvtalerMedMuligTilgang(avtaleRepository, query, Pageable.unpaged());
        verify(avtaleRepository).sokEtterAvtale(new NavIdent("Z000000"), 1, new Fnr("12345678901"), new BedriftNr("123456789"), "4802", null, null, false, Pageable.unpaged());

        query.setTiltakstype(Tiltakstype.ARBEIDSTRENING);
        query.setStatus(Status.MANGLER_GODKJENNING);
        veileder.hentAlleAvtalerMedMuligTilgang(avtaleRepository, query, Pageable.unpaged());
        verify(avtaleRepository).sokEtterAvtale(new NavIdent("Z000000"), 1, new Fnr("12345678901"), new BedriftNr("123456789"), "4802", Tiltakstype.ARBEIDSTRENING, Status.MANGLER_GODKJENNING, false, Pageable.unpaged());
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
