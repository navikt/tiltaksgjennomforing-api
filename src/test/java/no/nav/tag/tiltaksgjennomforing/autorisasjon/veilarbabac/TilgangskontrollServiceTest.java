package no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac;

import static no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService.NY_VEILEDERTILGANG;
import static org.assertj.core.api.Java6Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;

@RunWith(MockitoJUnitRunner.class)
public class TilgangskontrollServiceTest {

    private static final Fnr FNR = new Fnr("11111111111");
    private static final NavIdent NAV_IDENT = new NavIdent("X111111");

    @Mock
    private VeilarbabacClient veilarbabacClient;

    @Mock
    private FeatureToggleService featureToggleService;
    
    @Mock
    private TilgangskontrollAvslatt tilgangskontrollAvslatt; 

    @InjectMocks
    private TilgangskontrollService tilgangskontrollService;

    @Test
    public void harLesetilgangTilKandidat_skal_gi_empty_hvis_ny_veiledertilgang_er_avslått() {
        assertThat(tilgangskontrollService.harLesetilgangTilKandidat(NAV_IDENT, FNR).isEmpty()).isTrue();
    }
    
    @Test
    public void harLesetilgangTilKandidat_skal_gi_empty_hvis_ny_veiledertilgang_er_påslått_og_tilgangskontroll_avslått() {
        when(featureToggleService.isEnabled(NY_VEILEDERTILGANG)).thenReturn(true);
        when(tilgangskontrollAvslatt.tilgangskontrollAvslatt()).thenReturn(true);
        assertThat(tilgangskontrollService.harLesetilgangTilKandidat(NAV_IDENT, FNR).isEmpty()).isTrue();
    }

    @Test
    public void harLesetilgangTilKandidat_skal_gi_svar_fra_abac_hvis_ny_veiledertilgang_er_påslått_og_tilgangskontroll_ikke_er_avslått() {
        when(featureToggleService.isEnabled(NY_VEILEDERTILGANG)).thenReturn(true);
        when(tilgangskontrollAvslatt.tilgangskontrollAvslatt()).thenReturn(false);
        when(veilarbabacClient.sjekkTilgang(NAV_IDENT, FNR.asString(), TilgangskontrollAction.read)).thenReturn(true);
        assertThat(tilgangskontrollService.harLesetilgangTilKandidat(NAV_IDENT, FNR).get()).isTrue();
        verify(veilarbabacClient).sjekkTilgang(NAV_IDENT, FNR.asString(), TilgangskontrollAction.read);
    }
    
    @Test
    public void harSkrivetilgangTilKandidat_skal_gi_empty_hvis_ny_veiledertilgang_er_avslått() {
        assertThat(tilgangskontrollService.harSkrivetilgangTilKandidat(NAV_IDENT, FNR).isEmpty()).isTrue();
    }
    
    @Test
    public void harSkrivetilgangTilKandidat_skal_gi_empty_hvis_ny_veiledertilgang_er_påslått_og_tilgangskontroll_avslått() {
        when(featureToggleService.isEnabled(NY_VEILEDERTILGANG)).thenReturn(true);
        when(tilgangskontrollAvslatt.tilgangskontrollAvslatt()).thenReturn(true);
        assertThat(tilgangskontrollService.harSkrivetilgangTilKandidat(NAV_IDENT, FNR).isEmpty()).isTrue();
    }

    @Test
    public void harSkrivetilgangTilKandidat_skal_gi_svar_fra_abac_hvis_ny_veiledertilgang_er_påslått_og_tilgangskontroll_ikke_er_avslått() {
        when(featureToggleService.isEnabled(NY_VEILEDERTILGANG)).thenReturn(true);
        when(tilgangskontrollAvslatt.tilgangskontrollAvslatt()).thenReturn(false);
        when(veilarbabacClient.sjekkTilgang(NAV_IDENT, FNR.asString(), TilgangskontrollAction.update)).thenReturn(true);
        assertThat(tilgangskontrollService.harSkrivetilgangTilKandidat(NAV_IDENT, FNR).get()).isTrue();
        verify(veilarbabacClient).sjekkTilgang(NAV_IDENT, FNR.asString(), TilgangskontrollAction.update);
    }

    @Test
    public void sjekkLesetilgangTilKandidat_skal_gå_ok_hvis_ny_veiledertilgang_er_avslått() {
        when(featureToggleService.isEnabled(NY_VEILEDERTILGANG)).thenReturn(false);
        tilgangskontrollService.sjekkLesetilgangTilKandidat(NAV_IDENT, FNR);
    }
    
    @Test
    public void sjekkLesetilgangTilKandidat_skal_gå_ok_hvis_ny_veiledertilgang_er_påslått_og_tilgangskontroll_avslått() {
        when(featureToggleService.isEnabled(NY_VEILEDERTILGANG)).thenReturn(true);
        when(tilgangskontrollAvslatt.tilgangskontrollAvslatt()).thenReturn(true);
        tilgangskontrollService.sjekkLesetilgangTilKandidat(NAV_IDENT, FNR);
    }
    
    @Test
    public void sjekkLesetilgangTilKandidat_skal_gå_ok_hvis_ny_veiledertilgang_er_påslått_og_tilgangskontroll_påslått_og_abac_svarer_ok() {
        when(featureToggleService.isEnabled(NY_VEILEDERTILGANG)).thenReturn(true);
        when(tilgangskontrollAvslatt.tilgangskontrollAvslatt()).thenReturn(false);
        when(veilarbabacClient.sjekkTilgang(NAV_IDENT, FNR.asString(), TilgangskontrollAction.read)).thenReturn(true);
        tilgangskontrollService.sjekkLesetilgangTilKandidat(NAV_IDENT, FNR);
        verify(veilarbabacClient).sjekkTilgang(NAV_IDENT, FNR.asString(), TilgangskontrollAction.read);
    }
    
    @Test(expected=TilgangskontrollException.class)
    public void sjekkLesetilgangTilKandidat_skal_gi_exception_hvis_ny_veiledertilgang_er_påslått_og_tilgangskontroll_påslått_og_abac_svarer_ikke_ok() {
        when(featureToggleService.isEnabled(NY_VEILEDERTILGANG)).thenReturn(true);
        when(tilgangskontrollAvslatt.tilgangskontrollAvslatt()).thenReturn(false);
        when(veilarbabacClient.sjekkTilgang(NAV_IDENT, FNR.asString(), TilgangskontrollAction.read)).thenReturn(false);
        tilgangskontrollService.sjekkLesetilgangTilKandidat(NAV_IDENT, FNR);
        verify(veilarbabacClient).sjekkTilgang(NAV_IDENT, FNR.asString(), TilgangskontrollAction.read);
    }

    @Test
    public void sjekkSkrivetilgangTilKandidat_skal_gå_ok_hvis_ny_veiledertilgang_er_avslått() {
        when(featureToggleService.isEnabled(NY_VEILEDERTILGANG)).thenReturn(false);
        tilgangskontrollService.sjekkSkrivetilgangTilKandidat(NAV_IDENT, FNR);
    }
    
    @Test
    public void sjekkSkrivetilgangTilKandidat_skal_gå_ok_hvis_ny_veiledertilgang_er_påslått_og_tilgangskontroll_avslått() {
        when(featureToggleService.isEnabled(NY_VEILEDERTILGANG)).thenReturn(true);
        when(tilgangskontrollAvslatt.tilgangskontrollAvslatt()).thenReturn(true);
        tilgangskontrollService.sjekkSkrivetilgangTilKandidat(NAV_IDENT, FNR);
    }
    
    @Test
    public void sjekkSkrivetilgangTilKandidat_skal_gå_ok_hvis_ny_veiledertilgang_er_påslått_og_tilgangskontroll_påslått_og_abac_svarer_ok() {
        when(featureToggleService.isEnabled(NY_VEILEDERTILGANG)).thenReturn(true);
        when(tilgangskontrollAvslatt.tilgangskontrollAvslatt()).thenReturn(false);
        when(veilarbabacClient.sjekkTilgang(NAV_IDENT, FNR.asString(), TilgangskontrollAction.update)).thenReturn(true);
        tilgangskontrollService.sjekkSkrivetilgangTilKandidat(NAV_IDENT, FNR);
        verify(veilarbabacClient).sjekkTilgang(NAV_IDENT, FNR.asString(), TilgangskontrollAction.update);
    }
    
    @Test(expected=TilgangskontrollException.class)
    public void sjekkSkrivetilgangTilKandidat_skal_gi_exception_hvis_ny_veiledertilgang_er_påslått_og_tilgangskontroll_påslått_og_abac_svarer_ikke_ok() {
        when(featureToggleService.isEnabled(NY_VEILEDERTILGANG)).thenReturn(true);
        when(tilgangskontrollAvslatt.tilgangskontrollAvslatt()).thenReturn(false);
        when(veilarbabacClient.sjekkTilgang(NAV_IDENT, FNR.asString(), TilgangskontrollAction.update)).thenReturn(false);
        tilgangskontrollService.sjekkSkrivetilgangTilKandidat(NAV_IDENT, FNR);
        verify(veilarbabacClient).sjekkTilgang(NAV_IDENT, FNR.asString(), TilgangskontrollAction.update);
    }

}
