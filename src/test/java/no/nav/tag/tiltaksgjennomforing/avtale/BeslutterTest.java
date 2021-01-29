package no.nav.tag.tiltaksgjennomforing.avtale;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.exceptions.NavEnhetIkkeFunnetException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.AxsysService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;


class BeslutterTest {

  private TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
  private AvtaleRepository avtaleRepository = mock(AvtaleRepository.class);
  private AxsysService axsysService = mock(AxsysService.class);

  @Test
  public void hentAlleAvtalerMedMuligTilgang__hent_ingen_GODKJENTE_når_avtaler_har_gjeldende_tilskuddsperiodestatus_ubehandlet() {

    // GITT
    Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
    TilskuddPeriode tilskuddPeriode = new TilskuddPeriode();
    tilskuddPeriode.setStatus(TilskuddPeriodeStatus.GODKJENT);
    tilskuddPeriode.setBeløp(1200);
    tilskuddPeriode.setAvtaleInnhold(avtale.gjeldendeInnhold());
    avtale.gjeldendeInnhold().setTilskuddPeriode(Lists.list(tilskuddPeriode));

    Beslutter beslutter = new Beslutter(new NavIdent("J987654"), tilgangskontrollService, axsysService);

    AvtalePredicate avtalePredicate = new AvtalePredicate();
    avtalePredicate.setTilskuddPeriodeStatus(TilskuddPeriodeStatus.UBEHANDLET);

    // NÅR
    when(axsysService.hentEnheterNavAnsattHarTilgangTil(beslutter.getIdentifikator())).thenReturn(List.of(new NavEnhet(TestData.ENHET_OPPFØLGING)));
    when(avtaleRepository
        .finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheter(TilskuddPeriodeStatus.GODKJENT.name(), Set.of(TestData.ENHET_OPPFØLGING)))
        .thenReturn(Lists.list(avtale));
    List<Avtale> avtaler = beslutter.hentAlleAvtalerMedMuligTilgang(avtaleRepository, avtalePredicate);

    assertThat(avtaler).isEmpty();
  }

  @Test
  public void hentAlleAvtalerMedMuligTilgang__kan_hente_avtale_Med_godkjent_periode() {

    // GITT
    Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
    TilskuddPeriode tilskuddPeriode = new TilskuddPeriode();
    tilskuddPeriode.setStatus(TilskuddPeriodeStatus.GODKJENT);
    tilskuddPeriode.setBeløp(1200);
    tilskuddPeriode.setAvtaleInnhold(avtale.gjeldendeInnhold());
    avtale.gjeldendeInnhold().setTilskuddPeriode(Lists.list(tilskuddPeriode));

    Beslutter beslutter = new Beslutter(new NavIdent("J987654"), tilgangskontrollService, axsysService);

    AvtalePredicate avtalePredicate = new AvtalePredicate();
    avtalePredicate.setTilskuddPeriodeStatus(TilskuddPeriodeStatus.GODKJENT);

    // NÅR
    when(axsysService.hentEnheterNavAnsattHarTilgangTil(beslutter.getIdentifikator())).thenReturn(List.of(new NavEnhet(TestData.ENHET_OPPFØLGING)));
    when(avtaleRepository
        .finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheter(TilskuddPeriodeStatus.GODKJENT.name(), Set.of(TestData.ENHET_OPPFØLGING)))
        .thenReturn(Lists.list(avtale));
    List<Avtale> avtaler = beslutter.hentAlleAvtalerMedMuligTilgang(avtaleRepository, avtalePredicate);

    assertThat(avtaler).hasSize(1);
  }

  @Test
  public void hentAlleAvtalerMedMuligTilgang__kan_hente_kun_en_avtale_Med_to_ubehandlet_perioder() {

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

    Beslutter beslutter = new Beslutter(new NavIdent("J987654"), tilgangskontrollService, axsysService);

    AvtalePredicate avtalePredicate = new AvtalePredicate();
    avtalePredicate.setTilskuddPeriodeStatus(null);

    // NÅR
    when(axsysService.hentEnheterNavAnsattHarTilgangTil(beslutter.getIdentifikator())).thenReturn(List.of(new NavEnhet(TestData.ENHET_OPPFØLGING)));
    when(avtaleRepository
        .finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheter(TilskuddPeriodeStatus.UBEHANDLET.name(), Set.of(TestData.ENHET_OPPFØLGING)))
        .thenReturn(Lists.list(avtale));
    List<Avtale> avtales = beslutter.hentAlleAvtalerMedMuligTilgang(avtaleRepository, avtalePredicate);

    assertThat(avtales).hasSize(1);
  }

  @Test
  public void hentAlleAvtalerMedMuligTilgang__kaster_en_NAV_ENHET_IKKE_FUNNET_EXCEPTION_når_nav_enhet_er_tom_under_henting() {

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

    Beslutter beslutter = new Beslutter(new NavIdent("J987654"), tilgangskontrollService, axsysService);

    AvtalePredicate avtalePredicate = new AvtalePredicate();
    avtalePredicate.setTilskuddPeriodeStatus(null);

    // NÅR
    assertThrows(NavEnhetIkkeFunnetException.class, () -> {
      when(axsysService.hentEnheterNavAnsattHarTilgangTil(beslutter.getIdentifikator())).thenReturn(Collections.emptyList());

      List<Avtale> avtales = beslutter.hentAlleAvtalerMedMuligTilgang(avtaleRepository, avtalePredicate);
    });
  }


}