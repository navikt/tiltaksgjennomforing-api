package no.nav.tag.tiltaksgjennomforing;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.EndreAvtale;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TestDataTest {

  @Disabled("MAKES NO SENSE")
  @Test
  void endring_på_alle_TestData_endre_felter_så_ingen_er_Null_felter() {
    EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
    TestData.endreMaalInfo(endreAvtale);
    TestData.endreMentorInfo(endreAvtale);
    TestData.endreInkluderingstilskuddInfo(endreAvtale);
    assertThat(endreAvtale.getMaal()).isNotEmpty();
    assertThat(endreAvtale.getInkluderingstilskuddsutgift()).isNotEmpty();
    assertThat(endreAvtale).hasNoNullFieldsOrPropertiesExcept("vtao", "arbeidsgiverKid");
  }

  @Test
  void lokal_vtao_testdata_krever_oppfolging() {
    Avtale avtale = mock(Avtale.class);
    when(avtale.getTiltakstype()).thenReturn(Tiltakstype.VTAO);

    TestDataGenerator.settOppfolgingKrevesForVtao(avtale);

    verify(avtale).setKreverOppfolgingFom(Now.localDate().minusDays(1));
    verify(avtale).setOppfolgingVarselSendt(any());
  }

  @Test
  void lokal_vtao_testdata_krever_oppfolging_i_dag() {
    Avtale avtale = mock(Avtale.class);
    when(avtale.getTiltakstype()).thenReturn(Tiltakstype.VTAO);

    TestDataGenerator.settOppfolgingKrevesForVtao(avtale, Now.localDate());

    verify(avtale).setKreverOppfolgingFom(Now.localDate());
    verify(avtale).setOppfolgingVarselSendt(any());
  }
}
