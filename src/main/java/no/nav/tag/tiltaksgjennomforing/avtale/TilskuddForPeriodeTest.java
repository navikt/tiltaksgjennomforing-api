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
        assertEquals(61, (DAYS.between(list.get(0).getDatoFom(), list.get(0).getDatoTom()) + 1L));
        assertEquals(40667, list.get(0).getBeløp());
    }

    @Test
    public void beregnTilskuddFor10dagerISammeMåned() {

        final int månedslønn = 20000;
        LocalDate datoFom = LocalDate.of(2020, 1, 10);
        LocalDate datoTom = LocalDate.of(2020, 1, 20);

        List<TilskuddPeriode> list  = tilskuddForPeriode.beregnTilskuddForPeriode(månedslønn, datoFom, datoTom);
        assertEquals(11, (DAYS.between(list.get(0).getDatoFom(), list.get(0).getDatoTom()) + 1L));
        assertEquals(7333, list.get(0).getBeløp());

//        int refusjonForPeriode = tilskuddForPeriode.beregnTilskuddForPeriode(månedslønn, datoFom, datoTom);
//        assertEquals(6667, refusjonForPeriode);
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
    public void lager1ogEnHalvRefusjonsperioder() {
        LocalDate datoFom = LocalDate.of(2020, 1, 1);
        LocalDate datoTom = LocalDate.of(2020, 5, 15);
        LocalDate _31_MARS = LocalDate.of(2020, 3, 31);
        LocalDate _1_APRIL = LocalDate.of(2020, 4, 1);

        List<TilskuddPeriode> perioder = tilskuddForPeriode.beregnTilskuddForPeriode(20000, datoFom, datoTom);

        assertEquals(2, perioder.size());
        TilskuddPeriode tilskuddPeriode = perioder.get(0);
        assertEquals(datoFom, tilskuddPeriode.getDatoFom());
        assertEquals(_31_MARS, tilskuddPeriode.getDatoTom());

        assertEquals(60000, tilskuddPeriode.getBeløp());

        tilskuddPeriode = perioder.get(1);
        assertEquals(_1_APRIL, tilskuddPeriode.getDatoFom());
        assertEquals(datoTom, tilskuddPeriode.getDatoTom());

        assertEquals(45, DAYS.between(tilskuddPeriode.getDatoFom(), tilskuddPeriode.getDatoTom()) + 1L);

        assertEquals(30000, tilskuddPeriode.getBeløp());
    }

}
