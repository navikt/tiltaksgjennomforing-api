package no.nav.tag.tiltaksgjennomforing.enhet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({Miljø.LOCAL, "wiremock"})
@DirtiesContext
class VeilarbArenaClientTest {

  @Autowired
  private VeilarbArenaClient veilarbArenaClient;

  @Test
  public void hent_oppfølingsEnhet_fra_arena() {
    String oppfølgingsEnhet = veilarbArenaClient.hentOppfølgingsEnhet("12345678901");
    assertThat(oppfølgingsEnhet).isEqualTo("0906");
  }

  @Test
  public void finner_ikke_oppfølingsEnhet_for_fnr() {
    String oppfølgingsEnhet = veilarbArenaClient.hentOppfølgingsEnhet("33333333333");
    assertThat(oppfølgingsEnhet).isNull();
  }

//  @Test
//  public void hent_oppfølingsEnhet_fra_arena__kaster_exception_ved_500() {
//    assertThatThrownBy(() -> {
//      veilarbArenaClient.hentOppfølgingsEnhet("11111111111");
//    }).isInstanceOf(VeilarbArenaException.class);
//  }
//
//  @Test
//  public void hent_oppfølingsEnhet_fra_arena__kaster_exception_feil_url() {
//    assertThatThrownBy(() -> {
//      veilarbArenaClient.hentOppfølgingsEnhet("22222222222");
//    }).isInstanceOf(VeilarbArenaException.class);
//  }

}