package no.nav.tag.tiltaksgjennomforing.avtale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.exceptions.ErAlleredeVeilederException;
import no.nav.tag.tiltaksgjennomforing.exceptions.KanIkkeGodkjenneAvtalePåKode6Exception;
import no.nav.tag.tiltaksgjennomforing.exceptions.VeilederSkalGodkjenneSistException;
import no.nav.tag.tiltaksgjennomforing.persondata.PdlRespons;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import org.junit.Test;

public class VeilederTest {
    @Test(expected = VeilederSkalGodkjenneSistException.class)
    public void godkjennAvtale__kan_ikke_godkjenne_foerst() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.godkjennAvtale(avtale.getSistEndret(), avtale);
    }

    @Test
    public void godkjennAvtale__kan_godkjenne_sist() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.godkjennAvtale(avtale.getSistEndret(), avtale);
        assertThat(avtale.erGodkjentAvVeileder()).isTrue();
    }

    @Test(expected = KanIkkeGodkjenneAvtalePåKode6Exception.class)
    public void godkjennAvtale__kan_ikke_godkjenne_kode6() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        PersondataService persondataService = mock(PersondataService.class);
        when(persondataService.erKode6(avtale.getDeltakerFnr())).thenReturn(true);
        Veileder veileder = TestData.enVeileder(avtale, persondataService);
        veileder.godkjennAvtale(avtale.getSistEndret(), avtale);
    }

    @Test(expected = KanIkkeGodkjenneAvtalePåKode6Exception.class)
    public void godkjennForVeilederOgDeltaker__kan_ikke_godkjenne_kode6() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        PersondataService persondataService = mock(PersondataService.class);
        when(persondataService.erKode6(avtale.getDeltakerFnr())).thenReturn(true);
        Veileder veileder = TestData.enVeileder(avtale, persondataService);
        veileder.godkjennForVeilederOgDeltaker(TestData.enGodkjentPaVegneGrunn(), avtale);
    }

    @Test
    public void opphevGodkjenninger__kan_alltid_oppheve_godkjenninger() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvVeileder(LocalDateTime.now());
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.opphevGodkjenninger(avtale);
        assertThat(avtale.erGodkjentAvDeltaker()).isFalse();
        assertThat(avtale.erGodkjentAvArbeidsgiver()).isFalse();
        assertThat(avtale.erGodkjentAvVeileder()).isFalse();
    }

    @Test
    public void avbrytAvtale__kan_avbryt_avtale_etter_veiledergodkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvVeileder(LocalDateTime.now());
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.avbrytAvtale(avtale.getSistEndret(), new AvbruttInfo(LocalDate.now(), "enGrunn"), avtale);
        assertThat(avtale.isAvbrutt()).isTrue();
        assertThat(avtale.getAvbruttDato()).isNotNull();
        assertThat(avtale.getAvbruttGrunn()).isEqualTo("enGrunn");
    }

    @Test
    public void avbrytAvtale__kan_avbryte_avtale_foer_veiledergodkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.avbrytAvtale(avtale.getSistEndret(), new AvbruttInfo(LocalDate.now(), "enGrunn"), avtale);
        assertThat(avtale.isAvbrutt()).isTrue();
        assertThat(avtale.getAvbruttDato()).isNotNull();
        assertThat(avtale.getAvbruttGrunn()).isEqualTo("enGrunn");
    }

    @Test
    public void gjenopprettAvtale__kan_gjenopprette_avtale_etter_veiledergodkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvVeileder(LocalDateTime.now());
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        avtale.setAvbrutt(true);
        avtale.setAvbruttGrunn("enGrunn");
        avtale.setAvbruttDato(LocalDate.now());

        Veileder veileder = TestData.enVeileder(avtale);
        veileder.gjenopprettAvtale(avtale);
        assertThat(avtale.isAvbrutt()).isFalse();
        assertThat(avtale.getAvbruttDato()).isNull();
        assertThat(avtale.getAvbruttGrunn()).isNull();
    }

    @Test
    public void gjenopprettAvtale__kan_gjenopprette_avtale_foer_veiledergodkjenning() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        avtale.setAvbrutt(true);
        avtale.setAvbruttGrunn("enGrunn");
        avtale.setAvbruttDato(LocalDate.now());
        Veileder veileder = TestData.enVeileder(avtale);

        veileder.gjenopprettAvtale(avtale);
        assertThat(avtale.isAvbrutt()).isFalse();
        assertThat(avtale.getAvbruttDato()).isNull();
        assertThat(avtale.getAvbruttGrunn()).isNull();
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

    @Test(expected = ErAlleredeVeilederException.class)
    public void overtarAvtale__feil_hvis_samme_ident() {
        Avtale avtale = TestData.enAvtaleMedAltUtfylt();
        Veileder veileder = TestData.enVeileder(avtale);
        veileder.overtaAvtale(avtale);
    }

    @Test
    public void oprettAvtale__setter_startverdier_på_avtale() {
        OpprettAvtale opprettAvtale = new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        PersondataService persondataService = mock(PersondataService.class);
        Norg2Client norg2Client = mock(Norg2Client.class);
        final PdlRespons pdlRespons = TestData.enPdlrespons(false);

        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(TestData.enNavIdent()), eq(TestData.etFodselsnummer()))).thenReturn(true);
        when(persondataService.hentPersondata(TestData.etFodselsnummer())).thenReturn(pdlRespons);
        when(persondataService.erKode6Eller7(pdlRespons)).thenCallRealMethod();
        when(norg2Client.hentGeografiskEnhet(pdlRespons.getData().getHentGeografiskTilknytning().getGtBydel())).thenReturn(TestData.ENHET_GEOGRAFISK);

        Veileder veileder = new Veileder(TestData.enNavIdent(), tilgangskontrollService, persondataService, norg2Client,
            Set.of(TestData.ENHET_GEOGRAFISK));
        Avtale avtale = veileder.opprettAvtale(opprettAvtale);

        assertThat(avtale.getVeilederNavIdent()).isEqualTo(TestData.enNavIdent());
        assertThat(avtale.getDeltakerFornavn()).isEqualTo("Donald");
        assertThat(avtale.getDeltakerEtternavn()).isEqualTo("Duck");
        assertThat(avtale.getEnhetGeografisk()).isEqualTo(TestData.ENHET_GEOGRAFISK);
    }
}
