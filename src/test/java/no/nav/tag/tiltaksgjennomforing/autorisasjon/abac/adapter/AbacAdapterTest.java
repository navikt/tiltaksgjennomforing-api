package no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.adapter;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles({ Miljø.LOCAL, "wiremock" })
@DirtiesContext
public class AbacAdapterTest {

    @Autowired
    private AbacAdapter abacAdapter;

    @Test
    public void skal_teste_at_Abac_ikke_gi_lese_tilgang_på_På_Gitt_Bruker_Og_Veileder() {
        NavIdent veilederIdent = new NavIdent("F142226");
        Fnr deltakerFnr = new Fnr("01118023456");

        boolean verdic = abacAdapter.harLeseTilgang(veilederIdent, deltakerFnr);

        assertFalse(verdic);
    }

    @Test
    public void skal_teste_abac_feiler_gir_false() {
        NavIdent veilederIdent = new NavIdent("F142226");
        Fnr deltakerFnr = new Fnr("11111111111");

        boolean verdic = abacAdapter.harLeseTilgang(veilederIdent, deltakerFnr);
        assertThat(verdic).isFalse();
    }

    @Test
    public void skal_teste_at_Abac_gi_lese_tilgang_på_Gitt_Bruker_Og_Veileder() {
        NavIdent veilederIdent = new NavIdent("F142226");
        Fnr deltakerFnr = new Fnr("07098142678");

        boolean verdic = abacAdapter.harLeseTilgang(veilederIdent, deltakerFnr);
        assertTrue(verdic);
    }

    @Test
    public void skal_teste_at_Abac_ikke_gir_tilgang_til_feil_person_fra_cache() {
        NavIdent veilederIdent = new NavIdent("F142226");
        Fnr deltakerFnr = new Fnr("01118023456");

        boolean harIkkeTilgang = abacAdapter.harLeseTilgang(veilederIdent, deltakerFnr);

        assertFalse(harIkkeTilgang);

        NavIdent veilederSkalHaTilgang = new NavIdent("X142226");
        boolean harTilgang = abacAdapter.harLeseTilgang(veilederSkalHaTilgang, deltakerFnr);

        assertTrue(harTilgang);

    }

}