package no.nav.tag.tiltaksgjennomforing;

import no.nav.tag.tiltaksgjennomforing.domene.NavIdent;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TilgangskontrollException;
import org.junit.Test;

import java.util.Arrays;

public class TilgangUnderPiloteringTest {
    @Test
    public void sjekkTilgang__default_skal_ikke_feile() {
        new TilgangUnderPilotering().sjekkTilgang(new NavIdent("Q000111"));
    }

    @Test(expected = TilgangskontrollException.class)
    public void sjekkTilgang__enabled_skal_feile() {
        TilgangUnderPilotering tilgangUnderPilotering = new TilgangUnderPilotering();
        tilgangUnderPilotering.setEnabled(true);
        tilgangUnderPilotering.sjekkTilgang(new NavIdent("Q000111"));
    }

    @Test
    public void sjekkTilgang__enabled__og_gitt_bruker_tilgang() {
        TilgangUnderPilotering tilgangUnderPilotering = new TilgangUnderPilotering();
        tilgangUnderPilotering.setEnabled(true);
        tilgangUnderPilotering.setIdenter(Arrays.asList(new NavIdent("Q000111")));
        tilgangUnderPilotering.sjekkTilgang(new NavIdent("Q000111"));
    }
}