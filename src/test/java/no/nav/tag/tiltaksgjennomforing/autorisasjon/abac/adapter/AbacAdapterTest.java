package no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.adapter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
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

    boolean verdic = abacAdapter.harLeseTilgang(veilederIdent,deltakerFnr);

    assertFalse(verdic);
  }

  @Test
  public void skal_teste_abac_feiler_med_en_exception(){
    assertThrows(TilgangskontrollException.class,() -> {
      NavIdent veilederIdent = new NavIdent("F142226");
      Fnr deltakerFnr = new Fnr("11111111111");

      boolean verdic = abacAdapter.harLeseTilgang(veilederIdent,deltakerFnr);
    });
  }

  @Test
  public void skal_teste_at_Abac_gi_lese_tilgang_på_Gitt_Bruker_Og_Veileder() {
    NavIdent veilederIdent = new NavIdent("F142226");
    Fnr deltakerFnr = new Fnr("07098142678");

    boolean verdic = abacAdapter.harLeseTilgang(veilederIdent,deltakerFnr);

    assertTrue(verdic);
  }

}