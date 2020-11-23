package no.nav.tag.tiltaksgjennomforing.avtale;

import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TilskuddForAvtalePeriodeTest { //TODO Test på årsskifte

    private final int månedslønn = 20000;
    private final LocalDate _1_JANUAR = LocalDate.of(2020, 1, 1);

    @Test
    public void beregnTilskuddFor3KalenderMåneder() {
        LocalDate datoFom = _1_JANUAR;
        LocalDate datoTom = LocalDate.of(2020, 3, 31);

        List<TilskuddPeriode> list  = TilskuddForAvtalePeriode.beregnTilskuddForAvtalePeriode(månedslønn, datoFom, datoTom);
        assertEquals(60000, list.get(0).getBeløp());
    }

    @Test
    public void beregnTilskuddFra15JanuarTil15Mars() {
        LocalDate datoFom = LocalDate.of(2020, 1, 15);
        LocalDate datoTom = LocalDate.of(2020, 3, 15);

        List<TilskuddPeriode> list  = TilskuddForAvtalePeriode.beregnTilskuddForAvtalePeriode(månedslønn, datoFom, datoTom);
        assertEquals(61, TilskuddForAvtalePeriode.beregnetilskuddsperiode(list.get(0).getStartDato(), list.get(0).getSluttDato()));
        assertEquals(40657, list.get(0).getBeløp());
    }

    @Test
    public void beregnTilskuddFor10dagerISammeMåned() {
        LocalDate datoFom = LocalDate.of(2020, 1, 10);
        LocalDate datoTom = LocalDate.of(2020, 1, 20);

        List<TilskuddPeriode> list  = TilskuddForAvtalePeriode.beregnTilskuddForAvtalePeriode(månedslønn, datoFom, datoTom);
        assertEquals(11, TilskuddForAvtalePeriode.beregnetilskuddsperiode(list.get(0).getStartDato(), list.get(0).getSluttDato()));
        assertEquals(7228, list.get(0).getBeløp());
    }

    @Test
    public void lagerEnRefusjonsperiode() {
        LocalDate datoFom = _1_JANUAR;
        LocalDate datoTom = LocalDate.of(2020, 3, 31);

        List<TilskuddPeriode> perioder = TilskuddForAvtalePeriode.beregnTilskuddForAvtalePeriode(20000, datoFom, datoTom);
        assertEquals(1, perioder.size());
        TilskuddPeriode tilskuddPeriode = perioder.get(0);
        assertEquals(datoFom, tilskuddPeriode.getStartDato());
        assertEquals(datoTom, tilskuddPeriode.getSluttDato());
    }

    @Test
    public void sjekkAtBeregnettilskuddsperiodeReturnerer45dager() {
        LocalDate avtaleStartDato = LocalDate.of(2020, 4, 1);
        LocalDate avtaleSluttDato = LocalDate.of(2020, 5, 15);

        long antalltilskuddsdager = TilskuddForAvtalePeriode.beregnetilskuddsperiode(avtaleStartDato, avtaleSluttDato);
        assertEquals(45, antalltilskuddsdager);

        avtaleStartDato = LocalDate.of(2020, 5, 1);
        avtaleSluttDato = LocalDate.of(2020, 5, 15);
        antalltilskuddsdager = TilskuddForAvtalePeriode.beregnetilskuddsperiode(avtaleStartDato, avtaleSluttDato);
        assertEquals(15, antalltilskuddsdager);
    }

    @Test
    public void lag2RefusjonerOgEndag() {
        LocalDate avtaleStartDato = LocalDate.of(2020, 3, 1);
        LocalDate avtaleSluttDato = LocalDate.of(2020, 9, 1);
        assertEquals(185, TilskuddForAvtalePeriode.beregnetilskuddsperiode(avtaleStartDato, avtaleSluttDato));
        LocalDate _31_MAI = LocalDate.of(2020, 5, 31);
        LocalDate _1_JUNI = LocalDate.of(2020, 6, 1);
        LocalDate _31_AUG = LocalDate.of(2020, 8, 31);

        List<TilskuddPeriode> perioder = TilskuddForAvtalePeriode.beregnTilskuddForAvtalePeriode(20000, avtaleStartDato, avtaleSluttDato);
        assertEquals(3, perioder.size());

        TilskuddPeriode tilskuddPeriode = perioder.get(0);
        assertEquals(avtaleStartDato, tilskuddPeriode.getStartDato());
        assertEquals(_31_MAI, tilskuddPeriode.getSluttDato());
        assertEquals(60000, tilskuddPeriode.getBeløp());

        tilskuddPeriode = perioder.get(1);
        assertEquals(60000, tilskuddPeriode.getBeløp());
        assertEquals(_1_JUNI, tilskuddPeriode.getStartDato());
        assertEquals(_31_AUG, tilskuddPeriode.getSluttDato());

        tilskuddPeriode = perioder.get(2);
        assertEquals(avtaleSluttDato, tilskuddPeriode.getStartDato());
        assertEquals(1, TilskuddForAvtalePeriode.beregnetilskuddsperiode(tilskuddPeriode.getStartDato(), tilskuddPeriode.getSluttDato()));
        assertEquals(657, tilskuddPeriode.getBeløp());
    }


    @Test
    public void lager1ogEnHalvRefusjonsperioder() {
        LocalDate avtaleStartDato = LocalDate.of(2020, 1, 1);
        LocalDate avtaleSluttDato = LocalDate.of(2020, 5, 15);
        assertEquals(136, TilskuddForAvtalePeriode.beregnetilskuddsperiode(avtaleStartDato, avtaleSluttDato));
        LocalDate _31_MARS = LocalDate.of(2020, 3, 31);
        LocalDate _1_APRIL = LocalDate.of(2020, 4, 1);

        List<TilskuddPeriode> perioder = TilskuddForAvtalePeriode.beregnTilskuddForAvtalePeriode(20000, avtaleStartDato, avtaleSluttDato);

        assertEquals(2, perioder.size());
        TilskuddPeriode tilskuddPeriode = perioder.get(0);
        assertEquals(avtaleStartDato, tilskuddPeriode.getStartDato());
        assertEquals(_31_MARS, tilskuddPeriode.getSluttDato());

        assertEquals(60000, tilskuddPeriode.getBeløp());

        tilskuddPeriode = perioder.get(1);
        assertEquals(_1_APRIL, tilskuddPeriode.getStartDato());
        assertEquals(avtaleSluttDato, tilskuddPeriode.getSluttDato());
        assertEquals(45, TilskuddForAvtalePeriode.beregnetilskuddsperiode(tilskuddPeriode.getStartDato(), tilskuddPeriode.getSluttDato()));
        assertEquals(29856, tilskuddPeriode.getBeløp());
    }

    @Test
    public void beregnNyttTilskuddOverNyttår() {
        LocalDate avtaleStartDato = LocalDate.of(2020, 11, 1);
        LocalDate avtaleSluttDato = LocalDate.of(2021, 1, 31);
        LocalDate _31_DES = LocalDate.of(2020, 12, 31);
        LocalDate _1_JAN = LocalDate.of(2021, 1, 1);
        LocalDate _31_JAN = LocalDate.of(2021, 1, 31);
        List<TilskuddPeriode> perioder = TilskuddForAvtalePeriode.beregnTilskuddForEttÅr(20000, avtaleStartDato, avtaleSluttDato);
        assertEquals(2, perioder.size());

        TilskuddPeriode tilskuddPeriode = perioder.get(0);
        assertEquals(avtaleStartDato, tilskuddPeriode.getStartDato());
        assertEquals(_31_DES, tilskuddPeriode.getSluttDato());

        tilskuddPeriode = perioder.get(1);
        assertEquals(_1_JAN, tilskuddPeriode.getStartDato());
        assertEquals(_31_JAN, tilskuddPeriode.getSluttDato());

    }

    @Test
    public void beregnNyttTilskuddOver3årOgEnDag() {
        LocalDate avtaleStartDato = LocalDate.of(2020, 11, 1);
        LocalDate avtaleSluttDato = LocalDate.of(2023, 11, 1);
        LocalDate _1_JAN = LocalDate.of(2021, 1, 1);
        LocalDate _31_MARS = LocalDate.of(2021, 3, 31);

        List<TilskuddPeriode> perioder = TilskuddForAvtalePeriode.beregnTilskuddForEttÅr(20000, avtaleStartDato, avtaleSluttDato);
        TilskuddPeriode tilskuddPeriode = perioder.get(1);
        assertEquals(_1_JAN, tilskuddPeriode.getStartDato());
        assertEquals(_31_MARS, tilskuddPeriode.getSluttDato());

        _1_JAN = LocalDate.of(2022, 1, 1);
        _31_MARS = LocalDate.of(2022, 3, 31);
        tilskuddPeriode = perioder.get(5);
        assertEquals(_1_JAN, tilskuddPeriode.getStartDato());
        assertEquals(_31_MARS, tilskuddPeriode.getSluttDato());


        assertEquals(13, perioder.size());
    }
}
