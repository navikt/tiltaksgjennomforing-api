package no.nav.tag.tiltaksgjennomforing.domene;

import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.integrasjon.axsys.AxsysService;
import no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties.PilotProperties;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TilgangUnderPiloteringTest {

    private AxsysService axsysService;
    private PilotProperties pilotProperties;
    private TilgangUnderPilotering tilgangUnderPilotering;

    @Before
    public void setUp() {
        axsysService = mock(AxsysService.class);
        pilotProperties = new PilotProperties();
        pilotProperties.setEnabled(true);
        pilotProperties.setEnheter(asList(new NavEnhet("1111"), new NavEnhet("2222")));
        tilgangUnderPilotering = new TilgangUnderPilotering(pilotProperties, axsysService);
    }

    @Test
    public void sjekkTilgang__default_skal_ikke_feile() {
        pilotProperties.setEnabled(false);
        tilgangUnderPilotering.sjekkTilgang(new NavIdent("Q000111"));
    }

    @Test(expected = TilgangskontrollException.class)
    public void sjekkTilgang__enabled_skal_feile() {
        tilgangUnderPilotering.sjekkTilgang(new NavIdent("Q000111"));
    }

    @Test
    public void sjekkTilgang__enabled__og_gitt_bruker_tilgang() {
        pilotProperties.setIdenter(asList(new NavIdent("Q000111")));
        tilgangUnderPilotering.sjekkTilgang(new NavIdent("Q000111"));
    }

    @Test(expected = TilgangskontrollException.class)
    public void sjekkTilgang__enabled__og_gitt_bruker_har_ikke_tilgang_til_kontor_skal_feile() {
        when(axsysService.hentEnheterVeilederHarTilgangTil(new NavIdent("Q000111"))).thenReturn(asList(new NavEnhet("1234"), new NavEnhet("5678")));
        tilgangUnderPilotering.sjekkTilgang(new NavIdent("Q000111"));
    }

    @Test(expected = TilgangskontrollException.class)
    public void sjekkTilgang__enabled__og_gitt_bruker_har_ingen_kontor_skal_feile() {
        when(axsysService.hentEnheterVeilederHarTilgangTil(new NavIdent("Q000111"))).thenReturn(emptyList());
        tilgangUnderPilotering.sjekkTilgang(new NavIdent("Q000111"));
    }

    @Test
    public void sjekkTilgang__enabled__og_gitt_bruker_har_tilgang_til_kontor() {
        when(axsysService.hentEnheterVeilederHarTilgangTil(new NavIdent("Q000111"))).thenReturn(asList(new NavEnhet("1111"), new NavEnhet("5678")));
        tilgangUnderPilotering.sjekkTilgang(new NavIdent("Q000111"));
    }
}

