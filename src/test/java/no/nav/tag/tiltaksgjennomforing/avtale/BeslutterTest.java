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
  public void hentAlleAvtalerMedMuligTilgang_kan_hente_avtale_Med_godkjent_periode_null_i_queryParams_for_setErGodkjkentTilskuddPerioder() {

    // GITT
    Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
    TilskuddPeriode tilskuddPeriode = new TilskuddPeriode();
    tilskuddPeriode.setBeløp(1200);
    tilskuddPeriode.setAvtaleInnhold(avtale.gjeldendeInnhold());
    avtale.gjeldendeInnhold().setTilskuddPeriode(Lists.list(tilskuddPeriode));

    TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
    TilskuddPeriodeRepository tilskuddPeriodeRepository = mock(TilskuddPeriodeRepository.class);
    Beslutter veileder = new Beslutter(new NavIdent("J987654"), tilgangskontrollService, tilskuddPeriodeRepository);

    AvtaleRepository avtaleRepository = mock(AvtaleRepository.class);

    AvtalePredicate avtalePredicate = new AvtalePredicate();
    avtalePredicate.setErGodkjkentTilskuddPerioder(null);

    // NÅR
    when(tilskuddPeriodeRepository.findAllByGodkjentTidspunktIsNotNull()).thenReturn(avtale.gjeldendeInnhold().getTilskuddPeriode());
    List<Avtale> avtales = veileder.hentAlleAvtalerMedMuligTilgang(avtaleRepository, avtalePredicate);

    assertThat(avtales).isEmpty();
  }

  @Test
  public void hentAlleAvtalerMedMuligTilgang_kan_hente_avtale_Med_godkjent_periode() {

    // GITT
    Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
    TilskuddPeriode tilskuddPeriode = new TilskuddPeriode();
    tilskuddPeriode.setBeløp(1200);
    tilskuddPeriode.setAvtaleInnhold(avtale.gjeldendeInnhold());
    avtale.gjeldendeInnhold().setTilskuddPeriode(Lists.list(tilskuddPeriode));

    TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
    TilskuddPeriodeRepository tilskuddPeriodeRepository = mock(TilskuddPeriodeRepository.class);
    Beslutter veileder = new Beslutter(new NavIdent("J987654"), tilgangskontrollService, tilskuddPeriodeRepository);

    AvtaleRepository avtaleRepository = mock(AvtaleRepository.class);

    AvtalePredicate avtalePredicate = new AvtalePredicate();
    avtalePredicate.setErGodkjkentTilskuddPerioder(true);

    // NÅR
    when(tilskuddPeriodeRepository.findAllByGodkjentTidspunktIsNotNull()).thenReturn(avtale.gjeldendeInnhold().getTilskuddPeriode());
    List<Avtale> avtales = veileder.hentAlleAvtalerMedMuligTilgang(avtaleRepository, avtalePredicate);

    assertThat(avtales).isNotEmpty();
  }

  @Test
  public void hentAlleAvtalerMedMuligTilgang_kan_hente_avtale_Med_ubehandlet_periode() {

    // GITT
    Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
    TilskuddPeriode tilskuddPeriode = new TilskuddPeriode();
    tilskuddPeriode.setBeløp(1200);
    tilskuddPeriode.setAvtaleInnhold(avtale.gjeldendeInnhold());
    avtale.gjeldendeInnhold().setTilskuddPeriode(Lists.list(tilskuddPeriode));

    TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
    TilskuddPeriodeRepository tilskuddPeriodeRepository = mock(TilskuddPeriodeRepository.class);
    Beslutter veileder = new Beslutter(new NavIdent("J987654"), tilgangskontrollService, tilskuddPeriodeRepository);

    AvtaleRepository avtaleRepository = mock(AvtaleRepository.class);

    AvtalePredicate avtalePredicate = new AvtalePredicate();
    avtalePredicate.setErGodkjkentTilskuddPerioder(false);

    // NÅR
    when(tilskuddPeriodeRepository.findAllByGodkjentTidspunktIsNull()).thenReturn(avtale.gjeldendeInnhold().getTilskuddPeriode());
    List<Avtale> avtales = veileder.hentAlleAvtalerMedMuligTilgang(avtaleRepository, avtalePredicate);

    assertThat(avtales).isNotEmpty();
  }


}