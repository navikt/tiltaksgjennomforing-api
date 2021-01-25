package no.nav.tag.tiltaksgjennomforing.avtale;

import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class NyTilskuddForAvtalePeriodeTest {


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

        assertThat(avtale.getDatoForRedusertProsent()).isEqualTo(LocalDate.of(2021,7,1));
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

        assertThat(avtale.getDatoForRedusertProsent()).isEqualTo(LocalDate.of(2022,1,1));
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
        endreAvtale.setStartDato(LocalDate.of(2021,1,1));
        endreAvtale.setSluttDato(LocalDate.of(2031,1,1));
        endreAvtale.setSumLonnstilskudd(60);
        avtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER);

        assertThat(avtale.getTilskuddPeriode().get(avtale.getTilskuddPeriode().size() -1).getLonnstilskuddProsent()).isEqualTo(60);
        assertThat(avtale.getDatoForRedusertProsent()).isNull();
    }

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


    private void harAlleDageneIAvtalenperioden(List<TilskuddPeriode> tilskuddPerioder, LocalDate avtaleStart, LocalDate avtaleSlutt) {
        long antallDager = avtaleStart.until(avtaleSlutt.plusDays(1), ChronoUnit.DAYS);
        Set<LocalDate> dateSet = new HashSet<>();
        for (TilskuddPeriode periode : tilskuddPerioder) {
            Set<LocalDate> localDates = periode.getStartDato().datesUntil(periode.getSluttDato().plusDays(1)).collect(Collectors.toSet());
            dateSet.addAll(localDates);
        }
        if (dateSet.size() != antallDager) {
            fail("Ulikt antall dager i avtalen og tilskuddsperiodene");
        }

    }

    private void harOverlappendeDatoer(List<TilskuddPeriode> tilskuddPerioder) {
        Set<LocalDate> dateSet = new HashSet<>();
        for (TilskuddPeriode periode : tilskuddPerioder) {
            Set<LocalDate> localDates = periode.getStartDato().datesUntil(periode.getSluttDato().plusDays(1)).collect(Collectors.toSet());

            if (!Collections.disjoint(dateSet, localDates)) {
                fail("Det finnes overlappende datoer");
            } else {
                dateSet.addAll(localDates);
            }
        }
    }

}