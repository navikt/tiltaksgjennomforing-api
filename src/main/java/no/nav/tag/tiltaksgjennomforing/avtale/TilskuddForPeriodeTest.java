package no.nav.tag.tiltaksgjennomforing.avtale;

import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TilskuddForPeriodeTest {

    private TilskuddForPeriode tilskuddForPeriode = new TilskuddForPeriode();

    @Test
    public void beregnTilskuddFor3HeleMåneder() {

        final int månedslønn = 20000;
        LocalDate datoFom = LocalDate.of(2020, 1, 1);
        LocalDate datoTom = LocalDate.of(2020, 3, 31);

        List<TilskuddPeriode> list  = tilskuddForPeriode.beregnTilskuddForPeriode(månedslønn, datoFom, datoTom);
        assertEquals(60000, list.get(0).getBeløp());
    }

    @Test
    public void beregnTilskuddFra15JanuarTil15Mars() {

        final int månedslønn = 20000;
        LocalDate datoFom = LocalDate.of(2020, 1, 15);
        LocalDate datoTom = LocalDate.of(2020, 3, 15);

        List<TilskuddPeriode> list  = tilskuddForPeriode.beregnTilskuddForPeriode(månedslønn, datoFom, datoTom);
        assertEquals(61, tilskuddForPeriode.beregnetilskuddsperiode(list.get(0).getDatoFom(), list.get(0).getDatoTom()));
        assertEquals(40667, list.get(0).getBeløp());
    }

    @Test
    public void beregnTilskuddFor10dagerISammeMåned() {

        final int månedslønn = 20000;
        LocalDate datoFom = LocalDate.of(2020, 1, 10);
        LocalDate datoTom = LocalDate.of(2020, 1, 20);

        List<TilskuddPeriode> list  = tilskuddForPeriode.beregnTilskuddForPeriode(månedslønn, datoFom, datoTom);
        assertEquals(11, tilskuddForPeriode.beregnetilskuddsperiode(list.get(0).getDatoFom(), list.get(0).getDatoTom()));
        assertEquals(7333, list.get(0).getBeløp());
    }

    @Test
    public void lagerEnRefusjonsperiode() {
        LocalDate datoFom = LocalDate.of(2020, 1, 1);
        LocalDate datoTom = LocalDate.of(2020, 3, 31);

        List<TilskuddPeriode> perioder = tilskuddForPeriode.beregnTilskuddForPeriode(20000, datoFom, datoTom);
        assertEquals(1, perioder.size());
        TilskuddPeriode tilskuddPeriode = perioder.get(0);
        assertEquals(datoFom, tilskuddPeriode.getDatoFom());
        assertEquals(datoTom, tilskuddPeriode.getDatoTom());
    }

    @Test
    public void sjekkAtBeregnettilskuddsperiodeReturnerer45dager() {
        LocalDate avtaleStartDato = LocalDate.of(2020, 4, 1);
        LocalDate avtaleSluttDato = LocalDate.of(2020, 5, 15);
        long antalltilskuddsdager = tilskuddForPeriode.beregnetilskuddsperiode(avtaleStartDato, avtaleSluttDato);

        assertEquals(45, antalltilskuddsdager);

        avtaleStartDato = LocalDate.of(2020, 5, 1);
        avtaleSluttDato = LocalDate.of(2020, 5, 15);
        antalltilskuddsdager = tilskuddForPeriode.beregnetilskuddsperiode(avtaleStartDato, avtaleSluttDato);

        assertEquals(15, antalltilskuddsdager);
    }

    @Test
    public void lag2RefusjonerOgEndag() {
        LocalDate avtaleStartDato = LocalDate.of(2020, 3, 1);
        LocalDate avtaleSluttDato = LocalDate.of(2020, 9, 1);
        assertEquals(185, tilskuddForPeriode.beregnetilskuddsperiode(avtaleStartDato, avtaleSluttDato));
        LocalDate _31_MAI = LocalDate.of(2020, 5, 31);
        LocalDate _1_JUNI = LocalDate.of(2020, 6, 1);
        LocalDate _31_AUG = LocalDate.of(2020, 8, 31);

        List<TilskuddPeriode> perioder = tilskuddForPeriode.beregnTilskuddForPeriode(20000, avtaleStartDato, avtaleSluttDato);

        assertEquals(3, perioder.size());

        TilskuddPeriode tilskuddPeriode = perioder.get(0);
        assertEquals(avtaleStartDato, tilskuddPeriode.getDatoFom());
        assertEquals(_31_MAI, tilskuddPeriode.getDatoTom());
        assertEquals(60000, tilskuddPeriode.getBeløp());

        tilskuddPeriode = perioder.get(1);
        assertEquals(60000, tilskuddPeriode.getBeløp());
        assertEquals(_1_JUNI, tilskuddPeriode.getDatoFom());
        assertEquals(_31_AUG, tilskuddPeriode.getDatoTom());

        tilskuddPeriode = perioder.get(2);
        assertEquals(avtaleSluttDato, tilskuddPeriode.getDatoTom());
        assertEquals(1, tilskuddForPeriode.beregnetilskuddsperiode(tilskuddPeriode.getDatoFom(), tilskuddPeriode.getDatoTom()));
        assertEquals(667, tilskuddPeriode.getBeløp());
    }


    @Test
    public void lager1ogEnHalvRefusjonsperioder() {
        LocalDate avtaleStartDato = LocalDate.of(2020, 1, 1);
        LocalDate avtaleSluttDato = LocalDate.of(2020, 5, 15);
        assertEquals(136, tilskuddForPeriode.beregnetilskuddsperiode(avtaleStartDato, avtaleSluttDato));
        LocalDate _31_MARS = LocalDate.of(2020, 3, 31);
        LocalDate _1_APRIL = LocalDate.of(2020, 4, 1);

        List<TilskuddPeriode> perioder = tilskuddForPeriode.beregnTilskuddForPeriode(20000, avtaleStartDato, avtaleSluttDato);

        assertEquals(2, perioder.size());
        TilskuddPeriode tilskuddPeriode = perioder.get(0);
        assertEquals(avtaleStartDato, tilskuddPeriode.getDatoFom());
        assertEquals(_31_MARS, tilskuddPeriode.getDatoTom());

        assertEquals(60000, tilskuddPeriode.getBeløp());

        tilskuddPeriode = perioder.get(1);
        assertEquals(_1_APRIL, tilskuddPeriode.getDatoFom());
        assertEquals(avtaleSluttDato, tilskuddPeriode.getDatoTom());


        assertEquals(45, tilskuddForPeriode.beregnetilskuddsperiode(tilskuddPeriode.getDatoFom(), tilskuddPeriode.getDatoTom()));

        assertEquals(30000, tilskuddPeriode.getBeløp());
    }

}
