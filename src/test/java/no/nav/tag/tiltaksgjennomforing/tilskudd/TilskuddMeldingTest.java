package no.nav.tag.tiltaksgjennomforing.tilskudd;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
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

class TilskuddMeldingTest {

  @Test
  void skal_ikke_kunne_hente_fra_avtale_uten_Tilskuddperiode() {

    // GIVEN
    Fnr deltakerFnr = new Fnr("01234567890");
    NavIdent veilederNavIdent = new NavIdent("X123456");
    BedriftNr bedriftNr = new BedriftNr("000111222");
    Avtale avtale = Avtale.veilederOppretterAvtale(new OpprettAvtale(deltakerFnr, bedriftNr, Tiltakstype.ARBEIDSTRENING), veilederNavIdent);

    // WHEN
    assertThrows(TilskuddFeilException.class, () ->
    {
      TilskuddMelding tilskuddMelding = TilskuddMelding.fraAvtale(avtale);
    });

  }

  @Test
  void skal_kun_sende_godkjente_tilskudd_perioder() {

    // GIVEN
    final Fnr deltakerFnr = new Fnr("01234567890");
    final NavIdent veilederNavIdent = new NavIdent("X123456");
    final BedriftNr bedriftNr = new BedriftNr("000111222");
    Avtale avtale = Avtale.veilederOppretterAvtale(new OpprettAvtale(deltakerFnr, bedriftNr, Tiltakstype.ARBEIDSTRENING), veilederNavIdent);
    TilskuddPeriode tilskuddPeriode = new TilskuddPeriode();
    tilskuddPeriode.setId(UUID.randomUUID());
    tilskuddPeriode.setBeløp(1000);
    tilskuddPeriode.setStartDato(LocalDate.now().minusDays(1));
    tilskuddPeriode.setSluttDato(LocalDate.now());

    avtale.setTilskuddPeriode(List.of(tilskuddPeriode));

    // WHEN
    TilskuddMelding tilskuddMelding = TilskuddMelding.fraAvtale(avtale);


    // THEN
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(avtale.getDeltakerFnr()).isEqualTo(tilskuddMelding.getDeltakerFnr());
      softly.assertThat(avtale.getBedriftNavn()).isEqualTo(tilskuddMelding.getBedriftNavn());
      softly.assertThat(avtale.getBedriftNr()).isEqualTo(tilskuddMelding.getBedriftnummer());
      softly.assertThat(avtale.getAvtaleInnholdId()).isEqualTo(tilskuddMelding.getAvtaleInnholdId());
      softly.assertThat(avtale.gjeldendeInnhold().getTilskuddPeriode().stream().findFirst().get().getBeløp()).isEqualTo(1000);
      softly.assertThat(avtale.getStillingprosent()).isNull();
    });

  }
}