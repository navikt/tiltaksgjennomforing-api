package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import no.nav.tag.tiltaksgjennomforing.*;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.orgenhet.Organisasjon;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac.TilgangskontrollService;

import org.junit.Before;
import org.junit.Test;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Optional;

import java.util.Arrays;

public class InnloggetBrukerTest {

    private Fnr deltaker;
    private NavIdent navIdent;
    private Avtale avtale;
    private BedriftNr bedriftNr;
    private TilgangskontrollService tilgangskontrollService;

    @Before
    public void setup() {
        deltaker = new Fnr("10000000000");
        navIdent = new NavIdent("X100000");
        bedriftNr = new BedriftNr("12345678901");
        avtale = AvtaleFactory.nyAvtale(new OpprettAvtale(deltaker, bedriftNr, Tiltakstype.ARBEIDSTRENING), navIdent);
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
        assertThat(new InnloggetSelvbetjeningBruker(deltaker, emptyList()).harLeseTilgang(avtale)).isTrue();
    }

    @Test
    public void harTilgang__veileder_skal_ha_lesetilgang_til_avtale_hvis_toggle_er_av() {
        InnloggetNavAnsatt innloggetNavAnsatt = new InnloggetNavAnsatt(navIdent, tilgangskontrollService);
        when(tilgangskontrollService.harLesetilgangTilKandidat(innloggetNavAnsatt, avtale.getDeltakerFnr())).thenReturn(Optional.empty());
        assertThat(innloggetNavAnsatt.harLeseTilgang(avtale)).isTrue();
    }

    @Test
    public void harTilgang__veileder_skal_ha_lesetilgang_til_avtale_hvis_toggle_er_på_og_tilgangskontroll_er_ok() {
        InnloggetNavAnsatt innloggetNavAnsatt = new InnloggetNavAnsatt(navIdent, tilgangskontrollService);
        when(tilgangskontrollService.harLesetilgangTilKandidat(innloggetNavAnsatt, avtale.getDeltakerFnr())).thenReturn(Optional.of(true));

        assertThat(innloggetNavAnsatt.harLeseTilgang(avtale)).isTrue();
        verify(tilgangskontrollService).harLesetilgangTilKandidat(innloggetNavAnsatt, avtale.getDeltakerFnr());
    }
    
    @Test
    public void harTilgang__veileder_skal_ikke_ha_lesetilgang_til_avtale_hvis_toggle_er_på_og_tilgangskontroll_feiler() {
        InnloggetNavAnsatt innloggetNavAnsatt = new InnloggetNavAnsatt(navIdent, tilgangskontrollService);
        when(tilgangskontrollService.harLesetilgangTilKandidat(innloggetNavAnsatt, avtale.getDeltakerFnr())).thenReturn(Optional.of(false));

        assertThat(innloggetNavAnsatt.harLeseTilgang(avtale)).isFalse();
    }
    
    @Test
    public void harTilgang__veileder_skal_ha_skrivetilgang_til_avtale_hvis_toggle_er_av() {
        InnloggetNavAnsatt innloggetNavAnsatt = new InnloggetNavAnsatt(navIdent, tilgangskontrollService);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(innloggetNavAnsatt, avtale.getDeltakerFnr())).thenReturn(Optional.empty());
        assertThat(innloggetNavAnsatt.harSkriveTilgang(avtale)).isTrue();
    }

    @Test
    public void harTilgang__veileder_skal_ha_skrivetilgang_til_avtale_hvis_toggle_er_på_og_tilgangskontroll_er_ok() {
        InnloggetNavAnsatt innloggetNavAnsatt = new InnloggetNavAnsatt(navIdent, tilgangskontrollService);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(innloggetNavAnsatt, avtale.getDeltakerFnr())).thenReturn(Optional.of(true));

        assertThat(innloggetNavAnsatt.harSkriveTilgang(avtale)).isTrue();
        verify(tilgangskontrollService).harSkrivetilgangTilKandidat(innloggetNavAnsatt, avtale.getDeltakerFnr());
    }
    
    @Test
    public void harTilgang__veileder_skal_ikke_ha_skrivetilgang_til_avtale_hvis_toggle_er_på_og_tilgangskontroll_feiler() {
        InnloggetNavAnsatt innloggetNavAnsatt = new InnloggetNavAnsatt(navIdent, tilgangskontrollService);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(innloggetNavAnsatt, avtale.getDeltakerFnr())).thenReturn(Optional.of(false));
        
        assertThat(innloggetNavAnsatt.harSkriveTilgang(avtale)).isFalse();
    }
    
    @Test
    public void harTilgang__arbeidsgiver_skal_ikke_ha_tilgang_til_avtale() {
        assertThat(new InnloggetSelvbetjeningBruker(TestData.etFodselsnummer(), emptyList()).harLeseTilgang(avtale)).isFalse();
    }

    @Test
    public void harTilgang__ikkepart_veileder_skal_ikke_ha_lesetilgang_hvis_toggle_er_av() {
        assertThat(new InnloggetNavAnsatt(new NavIdent("X123456"), tilgangskontrollService).harLeseTilgang(avtale)).isFalse();
    }

    @Test
    public void harTilgang__ikkepart_veileder_skal_ikke_ha_skrivetilgang_hvis_toggle_er_av() {
        assertThat(new InnloggetNavAnsatt(new NavIdent("X123456"), tilgangskontrollService).harSkriveTilgang(avtale)).isFalse();
    }
    
    @Test
    public void harTilgang__ikkepart_selvbetjeningsbruker_skal_ikke_ha_tilgang() {
        assertThat(new InnloggetSelvbetjeningBruker(new Fnr("00000000001"), emptyList()).harLeseTilgang(avtale)).isFalse();
    }

    @Test
    public void harTilgang__arbeidsgiver_skal_kunne_representere_bedrift_uten_Fnr() {
        InnloggetSelvbetjeningBruker innloggetSelvbetjeningBruker = new InnloggetSelvbetjeningBruker(new Fnr("00000000009"), Arrays.asList(new Organisasjon(bedriftNr, "Testbutikken")));
        assertThat(innloggetSelvbetjeningBruker.harLeseTilgang(avtale)).isTrue();
    }
}