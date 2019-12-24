package no.nav.tag.tiltaksgjennomforing.autorisasjon.pilottilgang;

import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.pilottilgang.TilgangUnderPilotering;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TilgangUnderPiloteringTest {

    private TilgangUnderPilotering tilgangUnderPilotering;
    private FeatureToggleService featureToggleService;

    @Before
    public void setUp() {
        featureToggleService = mock(FeatureToggleService.class);
        tilgangUnderPilotering = new TilgangUnderPilotering(featureToggleService);
    }
    
    @Test
    public void sjekkTilgang_skal_gi_tilgang_for_ident() {
        when(featureToggleService.isEnabled(TilgangUnderPilotering.TAG_TILTAK_PILOTTILGANG_IDENT)).thenReturn(true);
        tilgangUnderPilotering.sjekkTilgang(new NavIdent("Q000111"));
    }
    
    @Test
    public void sjekkTilgang_skal_gi_tilgang_for_kontor() {
        when(featureToggleService.isEnabled(TilgangUnderPilotering.TAG_TILTAK_PILOTTILGANG_KONTOR)).thenReturn(true);
        tilgangUnderPilotering.sjekkTilgang(new NavIdent("Q000111"));
    }
    
    @Test(expected = TilgangskontrollException.class)
    public void sjekkTilgang_skal_feile_hvis_ikke_tilgang_for_ident_eller_kontor() {
        tilgangUnderPilotering.sjekkTilgang(new NavIdent("Q000111"));
    }
}

