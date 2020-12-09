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
class VeilarboppfolgingClientTest {

  @Autowired
  private VeilarboppfolgingClient veilarboppfolgingClient;

  @Test
  public void hent_oppfølingsEnhet_fra_arena() {
    Oppfølgingsstatus oppfølgingsstatus = veilarboppfolgingClient.hentOppfølgingsEnhet("12345678901");
    assertThat(oppfølgingsstatus.getOppfolgingsenhet()).isEqualTo("4806");
  }

  @Test
  public void hent_oppfølingsEnhet_fra_arena__kaster_exception_ved_500() {
    assertThatThrownBy(() -> {
      veilarboppfolgingClient.hentOppfølgingsEnhet("799999999");
    }).isInstanceOf(VeilarbArenaException.class);
  }

}