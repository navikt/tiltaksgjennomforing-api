package no.nav.tag.tiltaksgjennomforing;

import static org.assertj.core.api.Assertions.assertThat;

import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import org.junit.jupiter.api.Test;

class TestDataTest {

  @Test
  void endringPåAlleFelterSkalIkkeHaNullFelter() {
    assertThat(TestData.endringPåAlleFelter()).hasNoNullFieldsOrProperties();
  }
}