package no.nav.tag.tiltaksgjennomforing.avtale;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.exceptions.NavEnhetIkkeFunnetException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.AxsysService;
import org.junit.jupiter.api.Test;


class BeslutterTest {

    private TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
    private AvtaleRepository avtaleRepository = mock(AvtaleRepository.class);
    private AxsysService axsysService = mock(AxsysService.class);

    @Test
    public void hentAlleAvtalerMedMuligTilgang__hent_ingen_GODKJENTE_når_avtaler_har_gjeldende_tilskuddsperiodestatus_ubehandlet() {

        // GITT
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        TilskuddPeriode tilskuddPeriode = new TilskuddPeriode();
        tilskuddPeriode.setStatus(TilskuddPeriodeStatus.GODKJENT);
        tilskuddPeriode.setBeløp(1200);
        tilskuddPeriode.setAvtale(avtale);
        avtale.setTilskuddPeriode(new TreeSet<>(List.of(tilskuddPeriode)));

        Beslutter beslutter = new Beslutter(new NavIdent("J987654"), tilgangskontrollService, axsysService);
        Integer plussDato = ((int) ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.now().plusMonths(3)));
        AvtalePredicate avtalePredicate = new AvtalePredicate();
        avtalePredicate.setTilskuddPeriodeStatus(TilskuddPeriodeStatus.UBEHANDLET);

        // NÅR
        when(axsysService.hentEnheterNavAnsattHarTilgangTil(beslutter.getIdentifikator())).thenReturn(List.of(TestData.ENHET_OPPFØLGING));
        when(avtaleRepository
                .finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheterUbehandlet(
                        TilskuddPeriodeStatus.GODKJENT.name(),
                        Set.of(TestData.ENHET_OPPFØLGING.getVerdi()),
                        plussDato))
                .thenReturn(List.of(avtale));
        List<Avtale> avtaler = beslutter.hentAlleAvtalerMedMuligTilgang(avtaleRepository, avtalePredicate);

        assertThat(avtaler).isEmpty();
    }

    @Test
    public void hentAlleAvtalerMedMuligTilgang__kan_hente_avtale_Med_godkjent_periode() {

        // GITT
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        TilskuddPeriode tilskuddPeriode = new TilskuddPeriode();
        tilskuddPeriode.setStatus(TilskuddPeriodeStatus.GODKJENT);
        tilskuddPeriode.setBeløp(1200);
        tilskuddPeriode.setAvtale(avtale);
        tilskuddPeriode.setStartDato(LocalDate.now().minusMonths(3));
        avtale.setTilskuddPeriode(new TreeSet<>(List.of(tilskuddPeriode)));

        Integer plussDato = ((int) ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.now().plusMonths(3)));

        Beslutter beslutter = new Beslutter(new NavIdent("J987654"), tilgangskontrollService, axsysService);

        AvtalePredicate avtalePredicate = new AvtalePredicate();
        avtalePredicate.setTilskuddPeriodeStatus(TilskuddPeriodeStatus.GODKJENT);

        // NÅR
        when(axsysService.hentEnheterNavAnsattHarTilgangTil(beslutter.getIdentifikator())).thenReturn(List.of(TestData.ENHET_OPPFØLGING));
        when(avtaleRepository
                .finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheterGodkjent(
                        TilskuddPeriodeStatus.GODKJENT.name(),
                        Set.of(TestData.ENHET_OPPFØLGING.getVerdi()),
                        plussDato))
                .thenReturn(List.of(avtale));
        List<Avtale> avtaler = beslutter.finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheter(avtaleRepository, avtalePredicate, "startDato");

        assertThat(avtaler).hasSize(1);
    }

    @Test
    public void hentAlleAvtalerMedMuligTilgang__kan_hente_kun_en_avtale_Med_to_ubehandlet_perioder() {

        // GITT
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        TilskuddPeriode tilskuddPeriode = new TilskuddPeriode();
        tilskuddPeriode.setBeløp(1200);
        tilskuddPeriode.setStartDato(LocalDate.now().plusDays(14));
        tilskuddPeriode.setLøpenummer(1);
        tilskuddPeriode.setStatus(TilskuddPeriodeStatus.UBEHANDLET);
        tilskuddPeriode.setAvtale(avtale);
        tilskuddPeriode.setStartDato(LocalDate.now().minusMonths(2));

        TilskuddPeriode tilskuddPeriode2 = new TilskuddPeriode();
        tilskuddPeriode2.setBeløp(1250);
        tilskuddPeriode2.setStartDato(LocalDate.now().plusDays(14).plusMonths(1));
        tilskuddPeriode2.setLøpenummer(2);
        tilskuddPeriode2.setStatus(TilskuddPeriodeStatus.UBEHANDLET);
        tilskuddPeriode2.setAvtale(avtale);
        tilskuddPeriode2.setStartDato(LocalDate.now().minusMonths(1));

        avtale.setTilskuddPeriode(new TreeSet<>(List.of(tilskuddPeriode, tilskuddPeriode2)));

        Beslutter beslutter = new Beslutter(new NavIdent("J987654"), tilgangskontrollService, axsysService);
        Integer plussDato = ((int) ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.now().plusMonths(3)));
        AvtalePredicate avtalePredicate = new AvtalePredicate();
        avtalePredicate.setTilskuddPeriodeStatus(null);

        // NÅR
        when(axsysService.hentEnheterNavAnsattHarTilgangTil(beslutter.getIdentifikator())).thenReturn(List.of(TestData.ENHET_OPPFØLGING));
        when(avtaleRepository
                .finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheterUbehandlet(
                        TilskuddPeriodeStatus.UBEHANDLET.name(),
                        Set.of(TestData.ENHET_OPPFØLGING.getVerdi()),
                        plussDato))
                .thenReturn(List.of(avtale));

        List<Avtale> avtales = beslutter
                .finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheter(
                        avtaleRepository,
                        avtalePredicate,
                        "startDato");

        assertThat(avtales).hasSize(1);
    }

    @Test
    public void hentAlleAvtalerMedMuligTilgang__kaster_en_NAV_ENHET_IKKE_FUNNET_EXCEPTION_når_nav_enhet_er_tom_under_henting() {

        // GITT
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        TilskuddPeriode tilskuddPeriode = new TilskuddPeriode();
        tilskuddPeriode.setBeløp(1200);
        tilskuddPeriode.setStatus(TilskuddPeriodeStatus.UBEHANDLET);
        tilskuddPeriode.setAvtale(avtale);

        TilskuddPeriode tilskuddPeriode2 = new TilskuddPeriode();
        tilskuddPeriode2.setBeløp(1250);
        tilskuddPeriode2.setStatus(TilskuddPeriodeStatus.UBEHANDLET);
        tilskuddPeriode2.setAvtale(avtale);

        avtale.setTilskuddPeriode(new TreeSet<>(List.of(tilskuddPeriode, tilskuddPeriode2)));

        Beslutter beslutter = new Beslutter(new NavIdent("J987654"), tilgangskontrollService, axsysService);

        AvtalePredicate avtalePredicate = new AvtalePredicate();
        avtalePredicate.setTilskuddPeriodeStatus(null);

        // NÅR
        assertThrows(NavEnhetIkkeFunnetException.class, () -> {
            when(axsysService.hentEnheterNavAnsattHarTilgangTil(beslutter.getIdentifikator())).thenReturn(Collections.emptyList());

            List<Avtale> avtales = beslutter.finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheter(
                    avtaleRepository,
                    avtalePredicate,
                    null);
        });
    }

    @Test
    public void toggle_godkjent_for_etterregistrering() {

        //GITT
        Avtale avtale = Avtale.veilederOppretterAvtale(new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD), TestData.enNavIdent());
        EndreAvtale endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        endreAvtale.setStartDato(LocalDate.of(2021, 12, 12));
        endreAvtale.setSluttDato(LocalDate.of(2021, 12, 1).plusYears(1));
        Beslutter beslutter = TestData.enBeslutter(avtale);

        // NÅR
        beslutter.setOmAvtalenKanEtterregistreres(avtale);
        assertThat(avtale.isGodkjentForEtterregistrering()).isTrue();

        beslutter.setOmAvtalenKanEtterregistreres(avtale);
        assertThat(avtale.isGodkjentForEtterregistrering()).isFalse();
    }
}