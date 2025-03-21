package no.nav.tag.tiltaksgjennomforing;

import no.nav.tag.tiltaksgjennomforing.avtale.EndreAvtale;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestDataTest {

  @Test
  void endring_på_alle_TestData_endre_felter_så_ingen_er_Null_felter() {
    EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
    TestData.endreMaalInfo(endreAvtale);
    TestData.endreMentorInfo(endreAvtale);
    TestData.endreInkluderingstilskuddInfo(endreAvtale);
    TestData.endreFadderInfo(endreAvtale);
    assertThat(endreAvtale.getMaal()).isNotEmpty();
    assertThat(endreAvtale.getInkluderingstilskuddsutgift()).isNotEmpty();
    assertThat(endreAvtale).hasNoNullFieldsOrPropertiesExcept("vtao");
  }
}
