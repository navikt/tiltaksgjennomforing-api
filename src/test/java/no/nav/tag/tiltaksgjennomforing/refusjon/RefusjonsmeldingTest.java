package no.nav.tag.tiltaksgjennomforing.refusjon;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.BedriftNr;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.avtale.OpprettAvtale;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

class RefusjonsmeldingTest {

  @Test
  void fraAvtale_Uten_Tilskuddperiode() {

    // GIVEN
    Fnr deltakerFnr = new Fnr("01234567890");
    NavIdent veilederNavIdent = new NavIdent("X123456");
    BedriftNr bedriftNr = new BedriftNr("000111222");
    Avtale avtale = Avtale.veilederOppretterAvtale(new OpprettAvtale(deltakerFnr, bedriftNr, Tiltakstype.ARBEIDSTRENING), veilederNavIdent);

    // WHEN
    assertThrows(RuntimeException.class, () ->
    {
      Refusjonsmelding refusjonsmelding = Refusjonsmelding.fraAvtale(avtale);
    });

  }

  @Test
  void fraAvtale() {

    // GIVEN
    Fnr deltakerFnr = new Fnr("01234567890");
    NavIdent veilederNavIdent = new NavIdent("X123456");
    BedriftNr bedriftNr = new BedriftNr("000111222");
    Avtale avtale = Avtale.veilederOppretterAvtale(new OpprettAvtale(deltakerFnr, bedriftNr, Tiltakstype.ARBEIDSTRENING), veilederNavIdent);
    TilskuddPeriode tilskuddPeriode = new TilskuddPeriode();
    tilskuddPeriode.setId(UUID.randomUUID());
    tilskuddPeriode.setBeløp(1000);
    tilskuddPeriode.setStartDato(LocalDate.now().minusDays(1));
    tilskuddPeriode.setSluttDato(LocalDate.now());

    avtale.setTilskuddPeriode(List.of(tilskuddPeriode));

    // WHEN
    Refusjonsmelding refusjonsmelding = Refusjonsmelding.fraAvtale(avtale);


    // THEN
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(avtale.getDeltakerFnr().asString()).isEqualTo(refusjonsmelding.getDeltakerFnr());
      softly.assertThat(avtale.getBedriftNavn()).isEqualTo(refusjonsmelding.getBedrift());
      softly.assertThat(avtale.getBedriftNr().asString()).isEqualTo(refusjonsmelding.getBedriftnummer());
      softly.assertThat(avtale.getAvtaleInnholdId().toString()).isEqualTo(refusjonsmelding.getAvtaleInnholdId());
      softly.assertThat(avtale.gjeldendeInnhold().getTilskuddPeriode().stream().findFirst().get().getBeløp()).isEqualTo(1000);
      softly.assertThat(avtale.getStillingprosent()).isNull();
    });

  }
}