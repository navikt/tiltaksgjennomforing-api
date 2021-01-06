package no.nav.tag.tiltaksgjennomforing.avtale;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
    tilskuddPeriode.setBeløp(1200);
    tilskuddPeriode.setAvtaleInnhold(avtale.gjeldendeInnhold());
    avtale.gjeldendeInnhold().setTilskuddPeriode(Lists.list(tilskuddPeriode));

    avtale.setGodkjentAvVeileder(LocalDateTime.now());
    avtale.setGodkjentAvDeltaker(LocalDateTime.now());
    avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
    TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
    TilskuddPeriodeRepository tilskuddPeriodeRepository = mock(TilskuddPeriodeRepository.class);
    Beslutter veileder = new Beslutter(new NavIdent("J987654"), tilgangskontrollService, tilskuddPeriodeRepository);

    AvtaleRepository avtaleRepository = mock(AvtaleRepository.class);

    AvtalePredicate avtalePredicate = new AvtalePredicate();
    avtalePredicate.setErGodkjkentTilskudd(true);

    // NÅR
    when(tilskuddPeriodeRepository.findAllByGodkjentTidspunktIsNotNull()).thenReturn(avtale.gjeldendeInnhold().getTilskuddPeriode());
    List<Avtale> avtales = veileder.hentAlleAvtalerMedMuligTilgang(avtaleRepository, avtalePredicate);

    assertThat(avtales).isNotEmpty();
  }

}