package no.nav.tag.tiltaksgjennomforing.domene.autorisasjon;

import no.nav.tag.tiltaksgjennomforing.domene.*;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.integrasjon.veilarbabac.TilgangskontrollService;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


public class InnloggetBrukerTest {

    private Fnr deltaker;
    private NavIdent navIdent;
    private Avtale avtale;
    private BedriftNr bedriftNr;
    private FeatureToggleService featureToggleService;
    private TilgangskontrollService tilgangskontrollService;

    @Before
    public void setup() {
        deltaker = new Fnr("10000000000");
        navIdent = new NavIdent("X100000");
        bedriftNr = new BedriftNr("12345678901");
        avtale = Avtale.nyAvtale(new OpprettAvtale(deltaker, bedriftNr), navIdent);
        featureToggleService = mock(FeatureToggleService.class);
        tilgangskontrollService = mock(TilgangskontrollService.class);
    }

    @Test
    public void deltakerKnyttetTilAvtaleSkalHaDeltakerRolle() {
        Avtale avtale = TestData.enAvtale();
        InnloggetSelvbetjeningBruker selvbetjeningBruker = TestData.innloggetSelvbetjeningBrukerUtenOrganisasjon(TestData.enDeltaker(avtale));
        assertThat(selvbetjeningBruker.avtalepart(avtale)).isInstanceOf(Deltaker.class);
    }

    @Test
    public void arbeidsgiverKnyttetTilAvtaleSkalHaArbeidsgiverRolle() {
        Avtale avtale = TestData.enAvtale();
        InnloggetSelvbetjeningBruker selvbetjeningBruker = TestData.innloggetSelvbetjeningBrukerMedOrganisasjon(TestData.enArbeidsgiver(avtale));
        assertThat(selvbetjeningBruker.avtalepart(avtale)).isInstanceOf(Arbeidsgiver.class);
    }

    @Test
    public void veilederKnyttetTilAvtaleSkalHaVeilederRolle() {
        Avtale avtale = TestData.enAvtale();
        InnloggetNavAnsatt navAnsatt = TestData.innloggetNavAnsatt(TestData.enVeileder(avtale));
        assertThat(navAnsatt.avtalepart(avtale)).isInstanceOf(Veileder.class);
    }

    @Test
    public void harTilgang__deltaker_skal_ha_tilgang_til_avtale() {
        assertThat(new InnloggetSelvbetjeningBruker(deltaker).harLeseTilgang(avtale)).isTrue();
    }

    @Test
    public void harTilgang__veileder_skal_ha_lesetilgang_til_avtale_hvis_toggle_er_av() {
        assertThat(new InnloggetNavAnsatt(navIdent, featureToggleService, tilgangskontrollService).harLeseTilgang(avtale)).isTrue();
        verifyZeroInteractions(tilgangskontrollService);
    }

    @Test
    public void harTilgang__veileder_skal_ha_lesetilgang_til_avtale_hvis_toggle_er_på_og_tilgangskontroll_er_ok() {
        when(featureToggleService.isEnabled(FeatureToggleService.NY_VEILEDERTILGANG)).thenReturn(true);

        InnloggetNavAnsatt innloggetNavAnsatt = new InnloggetNavAnsatt(navIdent, featureToggleService, tilgangskontrollService);
        assertThat(innloggetNavAnsatt.harLeseTilgang(avtale)).isTrue();
        verify(tilgangskontrollService).sjekkLesetilgangTilKandidat(innloggetNavAnsatt, avtale.getDeltakerFnr());
    }
    
    @Test
    public void harTilgang__veileder_skal_ikke_ha_lesetilgang_til_avtale_hvis_toggle_er_på_og_tilgangskontroll_kaster_exception() {
        when(featureToggleService.isEnabled(FeatureToggleService.NY_VEILEDERTILGANG)).thenReturn(true);

        InnloggetNavAnsatt innloggetNavAnsatt = new InnloggetNavAnsatt(navIdent, featureToggleService, tilgangskontrollService);
        doThrow(TilgangskontrollException.class).when(tilgangskontrollService).sjekkLesetilgangTilKandidat(innloggetNavAnsatt, avtale.getDeltakerFnr());
        assertThat(innloggetNavAnsatt.harLeseTilgang(avtale)).isFalse();
    }
    
    @Test
    public void harTilgang__veileder_skal_ha_skrivetilgang_til_avtale_hvis_toggle_er_av() {
        assertThat(new InnloggetNavAnsatt(navIdent, featureToggleService, tilgangskontrollService).harSkriveTilgang(avtale)).isTrue();
        verifyZeroInteractions(tilgangskontrollService);
    }

    @Test
    public void harTilgang__veileder_skal_ha_skrivetilgang_til_avtale_hvis_toggle_er_på_og_tilgangskontroll_er_ok() {
        when(featureToggleService.isEnabled(FeatureToggleService.NY_VEILEDERTILGANG)).thenReturn(true);

        InnloggetNavAnsatt innloggetNavAnsatt = new InnloggetNavAnsatt(navIdent, featureToggleService, tilgangskontrollService);
        assertThat(innloggetNavAnsatt.harSkriveTilgang(avtale)).isTrue();
        verify(tilgangskontrollService).sjekkSkrivetilgangTilKandidat(innloggetNavAnsatt, avtale.getDeltakerFnr());
    }
    
    @Test
    public void harTilgang__veileder_skal_ikke_ha_skrivetilgang_til_avtale_hvis_toggle_er_på_og_tilgangskontroll_kaster_exception() {
        when(featureToggleService.isEnabled(FeatureToggleService.NY_VEILEDERTILGANG)).thenReturn(true);
        InnloggetNavAnsatt innloggetNavAnsatt = new InnloggetNavAnsatt(navIdent, featureToggleService, tilgangskontrollService);
        doThrow(TilgangskontrollException.class).when(tilgangskontrollService).sjekkSkrivetilgangTilKandidat(innloggetNavAnsatt, avtale.getDeltakerFnr());

        assertThat(innloggetNavAnsatt.harSkriveTilgang(avtale)).isFalse();
    }
    
    @Test
    public void harTilgang__arbeidsgiver_skal_ikke_ha_tilgang_til_avtale() {
        assertThat(new InnloggetSelvbetjeningBruker(TestData.etFodselsnummer()).harLeseTilgang(avtale)).isFalse();
    }

    @Test
    public void harTilgang__ikkepart_veileder_skal_ikke_ha_lesetilgang_hvis_toggle_er_av() {
        assertThat(new InnloggetNavAnsatt(new NavIdent("X123456"), featureToggleService, tilgangskontrollService).harLeseTilgang(avtale)).isFalse();
    }

    @Test
    public void harTilgang__ikkepart_veileder_skal_ikke_ha_skrivetilgang_hvis_toggle_er_av() {
        assertThat(new InnloggetNavAnsatt(new NavIdent("X123456"), featureToggleService, tilgangskontrollService).harSkriveTilgang(avtale)).isFalse();
    }
    
    @Test
    public void harTilgang__ikkepart_selvbetjeningsbruker_skal_ikke_ha_tilgang() {
        assertThat(new InnloggetSelvbetjeningBruker(new Fnr("00000000001")).harLeseTilgang(avtale)).isFalse();
    }

    @Test
    public void harTilgang__arbeidsgiver_skal_kunne_representere_bedrift_uten_Fnr() {
        InnloggetSelvbetjeningBruker innloggetSelvbetjeningBruker = new InnloggetSelvbetjeningBruker(new Fnr("00000000009"));
        innloggetSelvbetjeningBruker.getOrganisasjoner().add(new Organisasjon(bedriftNr, "Testbutikken"));
        assertThat(innloggetSelvbetjeningBruker.harLeseTilgang(avtale)).isTrue();
    }
}