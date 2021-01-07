package no.nav.tag.tiltaksgjennomforing.avtale;

import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TilskuddForAvtalePeriodeTest {

    private final int månedslønn = 20000;
    private final LocalDate _1_JANUAR = LocalDate.of(2020, 1, 1);
    private final LocalDate _31_DES = LocalDate.of(2020, 12, 31);

    @Test
    public void beregnTilskuddFor3KalenderMåneder() {
        LocalDate datoFom = _1_JANUAR;
        LocalDate datoTom = LocalDate.of(2020, 3, 31);

        List<TilskuddPeriode> list = TilskuddForAvtalePeriode.beregnTilskuddsperioderForAvtale(månedslønn, datoFom, datoTom);
        assertEquals(1, list.size());
        assertEquals(datoFom, list.get(0).getStartDato());
        assertEquals(datoTom, list.get(0).getSluttDato());
        assertEquals(60000, list.get(0).getBeløp());
    }

    @Test
    public void beregnTilskuddFra15JanuarTil15Mars() {
        LocalDate datoFom = LocalDate.of(2020, 1, 15);
        LocalDate datoTom = LocalDate.of(2020, 3, 15);

        List<TilskuddPeriode> list = TilskuddForAvtalePeriode.beregnTilskuddsperioderForAvtale(månedslønn, datoFom, datoTom);
        assertEquals(1, list.size());
        assertEquals(datoFom, list.get(0).getStartDato());
        assertEquals(datoTom, list.get(0).getSluttDato());
        assertEquals(40657, list.get(0).getBeløp());
        assertEquals(61, beregnetilskuddsperiode(list.get(0).getStartDato(), list.get(0).getSluttDato()));
    }

    @Test
    public void beregnTilskuddFor10dagerISammeMåned() {
        LocalDate datoFom = LocalDate.of(2020, 1, 10);
        LocalDate datoTom = LocalDate.of(2020, 1, 20);

        List<TilskuddPeriode> list = TilskuddForAvtalePeriode.beregnTilskuddsperioderForAvtale(månedslønn, datoFom, datoTom);
        assertEquals(11, beregnetilskuddsperiode(list.get(0).getStartDato(), list.get(0).getSluttDato()));
        assertEquals(7228, list.get(0).getBeløp());
    }

    @Test
    public void lagerEnRefusjonsperiode() {
        LocalDate datoFom = _1_JANUAR;
        LocalDate datoTom = LocalDate.of(2020, 3, 31);

        List<TilskuddPeriode> perioder = TilskuddForAvtalePeriode.beregnTilskuddsperioderForAvtale(20000, datoFom, datoTom);
        assertEquals(1, perioder.size());
        TilskuddPeriode tilskuddPeriode = perioder.get(0);
        assertEquals(datoFom, tilskuddPeriode.getStartDato());
        assertEquals(datoTom, tilskuddPeriode.getSluttDato());
    }

    @Test
    public void sjekkAtBeregnettilskuddsperiodeReturnerer45dager() {
        LocalDate avtaleStartDato = LocalDate.of(2020, 4, 1);
        LocalDate avtaleSluttDato = LocalDate.of(2020, 5, 15);

        long antalltilskuddsdager = beregnetilskuddsperiode(avtaleStartDato, avtaleSluttDato);
        assertEquals(45, antalltilskuddsdager);

        avtaleStartDato = LocalDate.of(2020, 5, 1);
        avtaleSluttDato = LocalDate.of(2020, 5, 15);
        antalltilskuddsdager = beregnetilskuddsperiode(avtaleStartDato, avtaleSluttDato);
        assertEquals(15, antalltilskuddsdager);
    }

    @Test
    public void lag2RefusjonerOgEndag() {
        LocalDate avtaleStartDato = LocalDate.of(2020, 3, 1);
        LocalDate avtaleSluttDato = LocalDate.of(2020, 9, 1);
        assertEquals(185, beregnetilskuddsperiode(avtaleStartDato, avtaleSluttDato));
        LocalDate _31_MAI = LocalDate.of(2020, 5, 31);
        LocalDate _1_JUNI = LocalDate.of(2020, 6, 1);
        LocalDate _31_AUG = LocalDate.of(2020, 8, 31);

        List<TilskuddPeriode> perioder = TilskuddForAvtalePeriode.beregnTilskuddsperioderForAvtale(20000, avtaleStartDato, avtaleSluttDato);

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
        assertEquals(1, beregnetilskuddsperiode(tilskuddPeriode.getStartDato(), tilskuddPeriode.getSluttDato()));
        assertEquals(657, tilskuddPeriode.getBeløp());
    }


    @Test
    public void lager1ogEnHalvRefusjonsperioder() {
        LocalDate avtaleStartDato = LocalDate.of(2020, 1, 1);
        LocalDate avtaleSluttDato = LocalDate.of(2020, 5, 15);
        assertEquals(136, beregnetilskuddsperiode(avtaleStartDato, avtaleSluttDato));
        LocalDate _31_MARS = LocalDate.of(2020, 3, 31);
        LocalDate _1_APRIL = LocalDate.of(2020, 4, 1);

        List<TilskuddPeriode> perioder = TilskuddForAvtalePeriode.beregnTilskuddsperioderForAvtale(20000, avtaleStartDato, avtaleSluttDato);

        assertEquals(2, perioder.size());
        TilskuddPeriode tilskuddPeriode = perioder.get(0);
        assertEquals(avtaleStartDato, tilskuddPeriode.getStartDato());
        assertEquals(_31_MARS, tilskuddPeriode.getSluttDato());
        assertEquals(60000, tilskuddPeriode.getBeløp());

        tilskuddPeriode = perioder.get(1);
        assertEquals(_1_APRIL, tilskuddPeriode.getStartDato());
        assertEquals(avtaleSluttDato, tilskuddPeriode.getSluttDato());
        assertEquals(45, beregnetilskuddsperiode(tilskuddPeriode.getStartDato(), tilskuddPeriode.getSluttDato())); //20000 + ()
        assertEquals(29856, tilskuddPeriode.getBeløp());
    }

    @Test
    public void beregnNyttTilskuddOverNyttår() {
        LocalDate avtaleStartDato = LocalDate.of(2020, 11, 1);
        LocalDate avtaleSluttDato = LocalDate.of(2021, 1, 31);

        LocalDate _1_JAN = _1_JANUAR.plusYears(1);
        LocalDate _31_JAN = LocalDate.of(2021, 1, 31);
        List<TilskuddPeriode> perioder = TilskuddForAvtalePeriode.beregnTilskuddsperioderForAvtale(20000, avtaleStartDato, avtaleSluttDato);
        assertEquals(2, perioder.size());

        TilskuddPeriode tilskuddPeriode = perioder.get(0);
        assertEquals(avtaleStartDato, tilskuddPeriode.getStartDato());
        assertEquals(_31_DES, tilskuddPeriode.getSluttDato());
        assertEquals(40000, tilskuddPeriode.getBeløp());

        tilskuddPeriode = perioder.get(1);
        assertEquals(_1_JAN, tilskuddPeriode.getStartDato());
        assertEquals(_31_JAN, tilskuddPeriode.getSluttDato());
        assertEquals(20000, tilskuddPeriode.getBeløp());
    }

    @Test
    public void beregnNyttTilskuddOver3årOg1Dag() {
        LocalDate avtaleStartDato = LocalDate.of(2020, 11, 1);
        LocalDate avtaleSluttDato = LocalDate.of(2023, 12, 1);

        List<TilskuddPeriode> perioder = TilskuddForAvtalePeriode.beregnTilskuddsperioderForAvtale(20000, avtaleStartDato, avtaleSluttDato);

        assertEquals(13, perioder.size());

        //1ste periode: 2 mnd
        TilskuddPeriode tilskuddPeriode = perioder.get(0);
        assertEquals(avtaleStartDato, tilskuddPeriode.getStartDato());
        assertEquals(_31_DES, tilskuddPeriode.getSluttDato());
        assertEquals(40000, tilskuddPeriode.getBeløp());

        assertEquals(11,
                perioder.stream()
                        .filter(tilskuddPeriode1 -> tilskuddPeriode1.getBeløp() == 60000
                                && MONTHS.between(tilskuddPeriode1.getStartDato(), tilskuddPeriode1.getSluttDato().plusDays(1)) == 3)
                        .count());

        tilskuddPeriode = perioder.get(12);

        //Siste periode: 2 mnd og en dag
        assertEquals(2,MONTHS.between(tilskuddPeriode.getStartDato(), tilskuddPeriode.getSluttDato().plusDays(1)));
        assertEquals(40657, tilskuddPeriode.getBeløp());
    }

    private long beregnetilskuddsperiode(LocalDate fraDato, LocalDate tilDato) {
        return DAYS.between(fraDato, tilDato.plusDays(1));
    }
}
