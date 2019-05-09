package no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties;

import no.nav.tag.tiltaksgjennomforing.domene.NavIdent;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TilgangskontrollException;
import org.junit.Test;

import java.util.Arrays;

public class PilotPropertiesTest {
    @Test
    public void sjekkTilgang__default_skal_ikke_feile() {
        new PilotProperties().sjekkTilgang(new NavIdent("Q000111"));
    }

    @Test(expected = TilgangskontrollException.class)
    public void sjekkTilgang__enabled_skal_feile() {
        PilotProperties tilgangUnderPilotering = new PilotProperties();
        tilgangUnderPilotering.setEnabled(true);
        tilgangUnderPilotering.sjekkTilgang(new NavIdent("Q000111"));
    }

    @Test
    public void sjekkTilgang__enabled__og_gitt_bruker_tilgang() {
        PilotProperties tilgangUnderPilotering = new PilotProperties();
        tilgangUnderPilotering.setEnabled(true);
        tilgangUnderPilotering.setIdenter(Arrays.asList(new NavIdent("Q000111")));
        tilgangUnderPilotering.sjekkTilgang(new NavIdent("Q000111"));
    }
}