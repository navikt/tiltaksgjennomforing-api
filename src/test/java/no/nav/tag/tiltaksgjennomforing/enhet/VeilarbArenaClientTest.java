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
    assertThat(oppfølgingsEnhet).isEqualTo("1416");
  }

  @Test
  public void hent_oppfølingsEnhet_fra_arena__kaster_exception_ved_500() {
    assertThatThrownBy(() -> {
      veilarbArenaClient.hentOppfølgingsEnhet("799999999");
    }).isInstanceOf(VeilarbArenaException.class);
  }

}