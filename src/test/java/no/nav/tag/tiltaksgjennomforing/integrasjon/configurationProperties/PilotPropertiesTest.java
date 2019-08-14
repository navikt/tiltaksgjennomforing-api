package no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties;

import no.nav.tag.tiltaksgjennomforing.domene.NavIdent;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.integrasjon.axsys.AxsysService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.TestConfiguration;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class PilotPropertiesTest {
    @Mock
    private AxsysService axsysService;
    @InjectMocks
    private PilotProperties pilotProperties;


    @Test
    public void sjekkTilgang__default_skal_ikke_feile() {
        //List<NavIdent> identer = Arrays.asList(new NavIdent("Q000111"));
        //when(axsysService.hentIdenter("0906")).thenReturn(Arrays.asList(new NavIdent("Q000111")));
        pilotProperties.setEnheter(Collections.singletonList("0906"));
        pilotProperties.sjekkTilgang(new NavIdent("Q000111"));
    }

    @Test(expected = TilgangskontrollException.class)
    public void sjekkTilgang__enabled_skal_feile() {
        pilotProperties.setEnabled(true);
        pilotProperties.sjekkTilgang(new NavIdent("Q000111"));
    }

    @Test
    public void sjekkTilgang__enabled__og_gitt_bruker_tilgang() {
        pilotProperties.setEnabled(true);
        pilotProperties.setIdenter(Arrays.asList(new NavIdent("Q000111")));
        pilotProperties.sjekkTilgang(new NavIdent("Q000111"));
    }
}