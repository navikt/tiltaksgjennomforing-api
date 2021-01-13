package no.nav.tag.tiltaksgjennomforing.avtale;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import no.nav.tag.tiltaksgjennomforing.TestData;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac.TilgangskontrollService;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;


class BeslutterTest {

  @Test
  public void hentAlleAvtalerMedMuligTilgang_kan_hente_avtale_Med_godkjent_periode() {

    // GITT
    Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
    TilskuddPeriode tilskuddPeriode = new TilskuddPeriode();
    tilskuddPeriode.setStatus(TilskuddPeriodeStatus.GODKJENT);
    tilskuddPeriode.setBeløp(1200);
    tilskuddPeriode.setAvtaleInnhold(avtale.gjeldendeInnhold());
    avtale.gjeldendeInnhold().setTilskuddPeriode(Lists.list(tilskuddPeriode));

    TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
    TilskuddPeriodeRepository tilskuddPeriodeRepository = mock(TilskuddPeriodeRepository.class);
    Beslutter veileder = new Beslutter(new NavIdent("J987654"), tilgangskontrollService, tilskuddPeriodeRepository);

    AvtaleRepository avtaleRepository = mock(AvtaleRepository.class);

    AvtalePredicate avtalePredicate = new AvtalePredicate();
    avtalePredicate.setTilskuddPeriodeStatus(TilskuddPeriodeStatus.GODKJENT);

    // NÅR
    when(tilskuddPeriodeRepository.findAllByStatus(TilskuddPeriodeStatus.GODKJENT)).thenReturn(avtale.gjeldendeInnhold().getTilskuddPeriode());
    List<Avtale> avtaler = veileder.hentAlleAvtalerMedMuligTilgang(avtaleRepository, avtalePredicate);

    assertThat(avtaler).isNotEmpty();
  }

  @Test
  public void hentAlleAvtalerMedMuligTilgang_kan_hente_kun_en_avtale_Med_to_ubehandlet_perioder() {

    // GITT
    Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
    TilskuddPeriode tilskuddPeriode = new TilskuddPeriode();
    tilskuddPeriode.setBeløp(1200);
    tilskuddPeriode.setStatus(TilskuddPeriodeStatus.UBEHANDLET);
    tilskuddPeriode.setAvtaleInnhold(avtale.gjeldendeInnhold());

    TilskuddPeriode tilskuddPeriode2 = new TilskuddPeriode();
    tilskuddPeriode2.setBeløp(1250);
    tilskuddPeriode2.setStatus(TilskuddPeriodeStatus.UBEHANDLET);
    tilskuddPeriode2.setAvtaleInnhold(avtale.gjeldendeInnhold());

    avtale.gjeldendeInnhold().setTilskuddPeriode(Lists.list(tilskuddPeriode, tilskuddPeriode2));

    TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
    TilskuddPeriodeRepository tilskuddPeriodeRepository = mock(TilskuddPeriodeRepository.class);
    Beslutter veileder = new Beslutter(new NavIdent("J987654"), tilgangskontrollService, tilskuddPeriodeRepository);

    AvtaleRepository avtaleRepository = mock(AvtaleRepository.class);

    AvtalePredicate avtalePredicate = new AvtalePredicate();
    avtalePredicate.setTilskuddPeriodeStatus(null);

    // NÅR
    when(tilskuddPeriodeRepository.findAllByStatus(TilskuddPeriodeStatus.UBEHANDLET)).thenReturn(avtale.gjeldendeInnhold().getTilskuddPeriode());
    List<Avtale> avtales = veileder.hentAlleAvtalerMedMuligTilgang(avtaleRepository, avtalePredicate);

    assertThat(avtales).isNotEmpty();
    assertThat(avtales.size()).isEqualTo(1);
  }


}