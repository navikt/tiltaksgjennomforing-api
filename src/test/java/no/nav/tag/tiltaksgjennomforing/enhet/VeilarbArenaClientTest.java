package no.nav.tag.tiltaksgjennomforing.enhet;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.OpprettAvtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({Miljø.LOCAL, "wiremock"})
@DirtiesContext
class VeilarbArenaClientTest {

  @Autowired
  private VeilarbArenaClient veilarbArenaClient;

  @Test
  public void hent_formidlingsgruppe() {
    String formidlingsgruppe = veilarbArenaClient.hentFormidlingsgruppe("12068702214");
    assertThat(formidlingsgruppe).isEqualTo("ARBS");
  }

  @Test
  public void hent_servicegruppe() {
    String servicegruppe = veilarbArenaClient.hentServicegruppe("30083516360");
    assertThat(servicegruppe).isEqualTo("BFORM");
  }

  @Test
  public void hent_oppfølingsEnhet_fra_arena() {
    String oppfølgingsEnhet = veilarbArenaClient.hentOppfølgingsEnhet("22095923112");
    assertThat(oppfølgingsEnhet).isEqualTo("0906");
  }

  @Test
  public void finner_ikke_oppfølingsEnhet_for_fnr() {
    String oppfølgingsEnhet = veilarbArenaClient.hentOppfølgingsEnhet("33333333333");
    assertThat(oppfølgingsEnhet).isNull();
    String oppfølgingsEnhet2 = veilarbArenaClient.hentOppfølgingsEnhet("11111111111");
    assertThat(oppfølgingsEnhet2).isNotEmpty();
  }
/*
    "formidlingsgruppe": "IARBS",
    "kvalifiseringsgruppe": "VURDI",
    "rettighetsgruppe": "IYT",
    "iservFraDato": null,
    "oppfolgingsenhet": "0393"
* */

  @Test
  public void sjekkAt_formidlingsgruppe_som_faller_utenfor_kaster_exception() {
    String fnr_har_formidlingsgruppe_med_kode_IJOBS = "12345678901";
      assertThatThrownBy(() -> veilarbArenaClient.sjekkOgHentOppfølgingStatus(
              new OpprettAvtale(
                      new Fnr(fnr_har_formidlingsgruppe_med_kode_IJOBS),
                      new BedriftNr("999999999"),
                      Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD))).isExactlyInstanceOf(FeilkodeException.class)
              .hasMessage(Feilkode.FORMIDLINGSGRUPPE_IKKE_RETTIGHET.name());
  }

  @Test
  public void sjekkAt_kvalifiseringsgruppe_som_faller_utenfor_kaster_exception() {
    String fnr_har_kvalifiseringsgruppe_med_kode_IVURD = "12345678902";
    assertThatThrownBy(() -> veilarbArenaClient.sjekkOgHentOppfølgingStatus(
            new OpprettAvtale(
                    new Fnr(fnr_har_kvalifiseringsgruppe_med_kode_IVURD),
                    new BedriftNr("999999999"),
                    Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD))).isExactlyInstanceOf(FeilkodeException.class)
            .hasMessage(Feilkode.SERVICEKODE_MANGLER.name());
  }

  @Test
  public void hent_oppfølging_status(){
    Oppfølgingsstatus oppfølgingStatus = veilarbArenaClient.hentOppfølgingStatus("01056210306");

    assertThat(oppfølgingStatus.getFormidlingsgruppe()).isEqualTo(("ARBS"));
    assertThat(oppfølgingStatus.getKvalifiseringsgruppe().getKvalifiseringskode()).isEqualTo(("BFORM"));
    assertThat(oppfølgingStatus.getOppfolgingsenhet()).isEqualTo(("0906"));
  }
}