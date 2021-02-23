package no.nav.tag.tiltaksgjennomforing.avtale;

import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class RegnUtTilskuddsperioderForAvtaleTest {


    @Test
    public void en_tilskuddsperiode() {
        LocalDate fra = LocalDate.of(2021, 1, 1);
        LocalDate til = LocalDate.of(2021, 3, 31);

        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setStartDato(fra);
        endreAvtale.setSluttDato(til);
        avtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER);

        assertThat(avtale.getTilskuddPeriode().size()).isEqualTo(1);
        assertThat(avtale.getTilskuddPeriode().get(0).getBeløp()).isEqualTo(avtale.getSumLonnstilskudd() * 3);
        harOverlappendeDatoer(avtale.getTilskuddPeriode());
        harAlleDageneIAvtalenperioden(avtale.getTilskuddPeriode(), fra, til);
    }

    @Test
    public void splitt_ved_nyttår() {
        LocalDate fra = LocalDate.of(2020, 12, 1);
        LocalDate til = LocalDate.of(2021, 1, 31);

        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setStartDato(fra);
        endreAvtale.setSluttDato(til);
        avtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER);
        List<TilskuddPeriode> tilskuddPerioder = avtale.getTilskuddPeriode();

        assertThat(tilskuddPerioder.size()).isEqualTo(2);
        assertThat(tilskuddPerioder.get(0).getBeløp()).isEqualTo(tilskuddPerioder.get(1).getBeløp());
        harOverlappendeDatoer(tilskuddPerioder);
        harAlleDageneIAvtalenperioden(tilskuddPerioder, fra, til);
    }

    @Test
    public void reduksjon_etter_6_mnd__30_prosent_lonnstilskudd() {
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setLonnstilskuddProsent(40);
        avtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER);
        assertThat(avtale.getTilskuddPeriode().get(2).getLonnstilskuddProsent()).isEqualTo(30);
        harOverlappendeDatoer(avtale.getTilskuddPeriode());
        harAlleDageneIAvtalenperioden(avtale.getTilskuddPeriode(), avtale.getStartDato(), avtale.getSluttDato());
    }

    @Test
    public void reduksjon_etter_12_mnd_60_prosent_lonnstilskudd() {
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setLonnstilskuddProsent(60);
        endreAvtale.setSluttDato(endreAvtale.getStartDato().plusMonths(13));
        avtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER);

        TilskuddPeriode sisteTilskuddsperiode = avtale.getTilskuddPeriode().get(avtale.getTilskuddPeriode().size() - 1);
        assertThat(sisteTilskuddsperiode.getLonnstilskuddProsent()).isEqualTo(50);
        harOverlappendeDatoer(avtale.getTilskuddPeriode());
        harAlleDageneIAvtalenperioden(avtale.getTilskuddPeriode(), avtale.getStartDato(), avtale.getSluttDato());
    }

    @Test
    public void splitt_etter_reduksjon_30_prosnt_lonnstilskudd() {
        LocalDate startDato = LocalDate.of(2020, 6, 30);
        LocalDate sluttDato = LocalDate.of(2021, 1, 2);
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();

        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setLonnstilskuddProsent(40);
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);

        avtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER);
        assertThat(avtale.getTilskuddPeriode().size()).isEqualTo(4);
        harOverlappendeDatoer(avtale.getTilskuddPeriode());
        harAlleDageneIAvtalenperioden(avtale.getTilskuddPeriode(), avtale.getStartDato(), avtale.getSluttDato());
    }

    @Test
    public void sjekk_at_reduksjon_skjer_etter_6_mnd_ved_40_prosent() {
        LocalDate startDato = LocalDate.of(2021, 1, 1);
        LocalDate sluttDato = LocalDate.of(2021, 7, 1);
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        endreAvtale.setLonnstilskuddProsent(40);
        avtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER);

        assertThat(avtale.getDatoForRedusertProsent()).isEqualTo(LocalDate.of(2021, 7, 1));
        harOverlappendeDatoer(avtale.getTilskuddPeriode());
        harAlleDageneIAvtalenperioden(avtale.getTilskuddPeriode(), avtale.getStartDato(), avtale.getSluttDato());
    }

    @Test
    public void sjekk_at_reduksjon_skjer_etter_12_mnd_ved_60_prosent() {
        LocalDate startDato = LocalDate.of(2021, 1, 1);
        LocalDate sluttDato = LocalDate.of(2022, 1, 1);
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        endreAvtale.setLonnstilskuddProsent(60);
        avtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER);

        assertThat(avtale.getDatoForRedusertProsent()).isEqualTo(LocalDate.of(2022, 1, 1));
        harOverlappendeDatoer(avtale.getTilskuddPeriode());
        harAlleDageneIAvtalenperioden(avtale.getTilskuddPeriode(), avtale.getStartDato(), avtale.getSluttDato());
    }

    @Test
    public void sjekk_at_ingen_redusering_under_1_år_60_prosent() {
        LocalDate startDato = LocalDate.of(2021, 1, 1);
        LocalDate sluttDato = LocalDate.of(2021, 12, 31);
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setStartDato(startDato);
        endreAvtale.setSluttDato(sluttDato);
        endreAvtale.setLonnstilskuddProsent(60);
        avtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER);

        assertThat(avtale.getDatoForRedusertProsent()).isNull();
        harOverlappendeDatoer(avtale.getTilskuddPeriode());
        harAlleDageneIAvtalenperioden(avtale.getTilskuddPeriode(), avtale.getStartDato(), avtale.getSluttDato());
    }

    @Test
    public void sjekk_at_varig_lonnstilskudd_ikke_reduserses() {
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype.VARIG_LONNSTILSKUDD);
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setStartDato(LocalDate.of(2021, 1, 1));
        endreAvtale.setSluttDato(LocalDate.of(2031, 1, 1));
        endreAvtale.setLonnstilskuddProsent(60);
        avtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER);

        assertThat(avtale.getTilskuddPeriode().get(avtale.getTilskuddPeriode().size() - 1).getLonnstilskuddProsent()).isEqualTo(60);
        assertThat(avtale.getDatoForRedusertProsent()).isNull();
    }

    @Test
    public void sjekk_at_utbetalte_perioder_beholdes_ved_endring() {
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype.VARIG_LONNSTILSKUDD);
        avtale.getTilskuddPeriode().get(0).setStatus(TilskuddPeriodeStatus.UTBETALT);
        UUID idPåUtbetaltTilskuddsperiode = avtale.getTilskuddPeriode().get(0).getId();
        Integer beløpPåUtbetaltTilskuddsperiode = avtale.getTilskuddPeriode().get(0).getBeløp();
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setManedslonn(99999);
        avtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER);

        assertThat(avtale.getTilskuddPeriode().get(0).getStatus()).isEqualTo(TilskuddPeriodeStatus.UTBETALT);
        assertThat(avtale.getTilskuddPeriode().get(0).getId()).isEqualTo(idPåUtbetaltTilskuddsperiode);
        assertThat(avtale.getTilskuddPeriode().get(0).getBeløp()).isEqualTo(beløpPåUtbetaltTilskuddsperiode);
        harOverlappendeDatoer(avtale.getTilskuddPeriode());
        harAlleDageneIAvtalenperioden(avtale.getTilskuddPeriode(), avtale.getStartDato(), avtale.getSluttDato());
    }

    @Test
    public void sjekk_at_nye_perioder_ved_forlengelse_starter_etter_utbetalte_perioder() {
        Avtale avtale = TestData.enLønnstilskuddsAvtaleMedStartOgSlutt(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 4, 1));
        avtale.opphevGodkjenningerSomVeileder();


        avtale.getTilskuddPeriode().get(0).setStatus(TilskuddPeriodeStatus.UTBETALT);
        avtale.getTilskuddPeriode().get(1).setStatus(TilskuddPeriodeStatus.UTBETALT);


        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setStartDato(avtale.getStartDato());
        endreAvtale.setSluttDato(avtale.getSluttDato().plusMonths(3));
        avtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER);


        assertThat(avtale.getTilskuddPeriode().get(0).getStatus()).isEqualTo(TilskuddPeriodeStatus.UTBETALT);
        assertThat(avtale.getTilskuddPeriode().get(1).getStatus()).isEqualTo(TilskuddPeriodeStatus.UTBETALT);

        assertThat(avtale.getTilskuddPeriode().get(1).getStartDato()).isEqualTo(avtale.getTilskuddPeriode().get(0).getSluttDato().plusDays(1));
        assertThat(avtale.getTilskuddPeriode().get(2).getStatus()).isEqualTo(TilskuddPeriodeStatus.UBEHANDLET);

        harOverlappendeDatoer(avtale.getTilskuddPeriode());
        harAlleDageneIAvtalenperioden(avtale.getTilskuddPeriode(), avtale.getStartDato(), avtale.getSluttDato());
    }

    @Test
    public void sjekk_at_godkjent_perioder_beholdes_ved_endring_som_ikke_påvirker_økonomi() {
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype.VARIG_LONNSTILSKUDD);
        avtale.getTilskuddPeriode().get(0).setStatus(TilskuddPeriodeStatus.GODKJENT);
        UUID idPåGodkjentTilskuddsperiode = avtale.getTilskuddPeriode().get(0).getId();
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setSluttDato(avtale.getSluttDato().plusDays(1));
        avtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER);

        assertThat(avtale.getTilskuddPeriode().get(0).getStatus()).isEqualTo(TilskuddPeriodeStatus.GODKJENT);
        assertThat(avtale.getTilskuddPeriode().get(0).getId()).isEqualTo(idPåGodkjentTilskuddsperiode);
        harOverlappendeDatoer(avtale.getTilskuddPeriode());
        harAlleDageneIAvtalenperioden(avtale.getTilskuddPeriode(), avtale.getStartDato(), avtale.getSluttDato());
    }

    @Test
    public void sjekk_at_godkjent_perioder_ikke_beholdes_ved_endring_som_påvirker_økonomi() {
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype.VARIG_LONNSTILSKUDD);
        avtale.getTilskuddPeriode().get(0).setStatus(TilskuddPeriodeStatus.GODKJENT);
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();
        endreAvtale.setManedslonn(99999);
        avtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER);

        assertThat(avtale.getTilskuddPeriode().get(0).getStatus()).isEqualTo(TilskuddPeriodeStatus.ANNULLERT);
        harOverlappendeDatoer(avtale.getTilskuddPeriode());
        harAlleDageneIAvtalenperioden(avtale.getTilskuddPeriode(), avtale.getStartDato(), avtale.getSluttDato());
    }

    @Test
    public void sjekk_at_godkjent_periode_annulleres_ved_forkorting() {
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype.VARIG_LONNSTILSKUDD);
        avtale.getTilskuddPeriode().get(0).setStatus(TilskuddPeriodeStatus.GODKJENT);
        UUID idPåGodkjentTilskuddsperiode = avtale.getTilskuddPeriode().get(0).getId();
        EndreAvtale endreAvtale = TestData.endringPåAlleFelter();

        endreAvtale.setSluttDato(avtale.getStartDato().plusDays(1));
        avtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER);

        assertThat(avtale.getTilskuddPeriode().get(0).getStatus()).isEqualTo(TilskuddPeriodeStatus.ANNULLERT);
        assertThat(avtale.getTilskuddPeriode().get(0).getId()).isEqualTo(idPåGodkjentTilskuddsperiode);

        harOverlappendeDatoer(avtale.getTilskuddPeriode());
        harAlleDageneIAvtalenperioden(avtale.getTilskuddPeriode(), avtale.getStartDato(), avtale.getSluttDato());
    }


    /* ------------ Metoder som kun brukes innad i denne test-klassen ------------ */

    private void harAlleDageneIAvtalenperioden(List<TilskuddPeriode> tilskuddPerioder, LocalDate avtaleStart, LocalDate avtaleSlutt) {
        long antallDager = avtaleStart.until(avtaleSlutt.plusDays(1), ChronoUnit.DAYS);
        Set<LocalDate> dateSet = new HashSet<>();
        List<TilskuddPeriode> tilskuddsperioderUtenAnnullerte = tilskuddPerioder.stream().filter(tilskuddPeriode -> tilskuddPeriode.getStatus() != TilskuddPeriodeStatus.ANNULLERT).collect(Collectors.toList());
        for (TilskuddPeriode periode : tilskuddsperioderUtenAnnullerte) {
            Set<LocalDate> localDates = periode.getStartDato().datesUntil(periode.getSluttDato().plusDays(1)).collect(Collectors.toSet());
            dateSet.addAll(localDates);
        }
        if (dateSet.size() != antallDager) {
            fail("Ulikt antall dager i avtalen og tilskuddsperiodene");
        }

    }

    private void harOverlappendeDatoer(List<TilskuddPeriode> tilskuddPerioder) {
        Set<LocalDate> dateSet = new HashSet<>();
        List<TilskuddPeriode> tilskuddsperioderUtenAnnullerte = tilskuddPerioder.stream().filter(tilskuddPeriode -> tilskuddPeriode.getStatus() != TilskuddPeriodeStatus.ANNULLERT).collect(Collectors.toList());
        for (TilskuddPeriode periode : tilskuddsperioderUtenAnnullerte) {
            Set<LocalDate> localDates = periode.getStartDato().datesUntil(periode.getSluttDato().plusDays(1)).collect(Collectors.toSet());

            if (!Collections.disjoint(dateSet, localDates)) {
                fail("Det finnes overlappende datoer");
            } else {
                dateSet.addAll(localDates);
            }
        }
    }


    /* ------------ Tester av metoder som kun brukes innad i denne test-klassen ------------ */

    @Test
    public void sjekk_at_ikkeoverlappende_periode_ikke_har_overlappende_datoer() {
        TilskuddPeriode tilskuddPeriode1 = new TilskuddPeriode(1000, LocalDate.of(2021, 1, 1), LocalDate.of(2021, 3, 31), 60);
        TilskuddPeriode tilskuddPeriode2 = new TilskuddPeriode(1000, LocalDate.of(2021, 4, 1), LocalDate.of(2021, 6, 1), 60);
        harOverlappendeDatoer(List.of(tilskuddPeriode1, tilskuddPeriode2));
    }

    @Test(expected = AssertionError.class)
    public void sjekk_at_har_overlappende_datoer() {
        TilskuddPeriode tilskuddPeriode1 = new TilskuddPeriode(1000, LocalDate.of(2021, 1, 1), LocalDate.of(2021, 3, 31), 60);
        TilskuddPeriode tilskuddPeriode2 = new TilskuddPeriode(1000, LocalDate.of(2021, 3, 31), LocalDate.of(2021, 6, 1), 60);
        harOverlappendeDatoer(List.of(tilskuddPeriode1, tilskuddPeriode2));
    }

    @Test
    public void sjekk_at_alle_dagene_i_avtalen_er_i_tilskuddsperiodene() {
        TilskuddPeriode tilskuddPeriode1 = new TilskuddPeriode(1000, LocalDate.of(2021, 1, 1), LocalDate.of(2021, 3, 31), 60);
        TilskuddPeriode tilskuddPeriode2 = new TilskuddPeriode(1000, LocalDate.of(2021, 4, 1), LocalDate.of(2021, 6, 1), 60);
        harAlleDageneIAvtalenperioden(List.of(tilskuddPeriode1, tilskuddPeriode2), LocalDate.of(2021, 1, 1), LocalDate.of(2021, 6, 1));
    }

    @Test(expected = AssertionError.class)
    public void sjekk_at_det_feiler_nar_ikke_alle_dagene_i_avtalen_er_i_tilskuddsperiodene() {
        TilskuddPeriode tilskuddPeriode1 = new TilskuddPeriode(1000, LocalDate.of(2021, 1, 1), LocalDate.of(2021, 3, 31), 60);
        TilskuddPeriode tilskuddPeriode2 = new TilskuddPeriode(1000, LocalDate.of(2021, 4, 1), LocalDate.of(2021, 5, 25), 60);
        harAlleDageneIAvtalenperioden(List.of(tilskuddPeriode1, tilskuddPeriode2), LocalDate.of(2021, 1, 1), LocalDate.of(2021, 5, 26));
    }
}