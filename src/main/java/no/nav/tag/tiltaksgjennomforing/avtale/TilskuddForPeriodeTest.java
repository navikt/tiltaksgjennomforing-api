package no.nav.tag.tiltaksgjennomforing.avtale;

import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TilskuddForPeriodeTest {

    private TilskuddForPeriode tilskuddForPeriode = new TilskuddForPeriode();

    @Test
    public void beregnTilskuddFor3HeleMåneder() {

        final BigDecimal månedslønn = BigDecimal.valueOf(20000);
        LocalDate datoFom = LocalDate.of(2020, 1, 1);
        LocalDate datoTom = LocalDate.of(2020, 3, 31);

        int refusjonForPeriode = tilskuddForPeriode.beregnTilskuddForPeriode(månedslønn, datoFom, datoTom);
        assertEquals(60000, refusjonForPeriode);
    }

    @Test
    public void beregnTilskuddFra15JanuarTil15Mars() {

        final BigDecimal månedslønn = BigDecimal.valueOf(20000);
        LocalDate datoFom = LocalDate.of(2020, 1, 15);
        LocalDate datoTom = LocalDate.of(2020, 3, 15);

        int refusjonForPeriode = tilskuddForPeriode.beregnTilskuddForPeriode(månedslønn, datoFom, datoTom);
        assertEquals(40000, refusjonForPeriode);
    }

    @Test
    public void beregnTilskuddFor10dagerISammeMåned() {

        final BigDecimal månedslønn = BigDecimal.valueOf(20000);
        LocalDate datoFom = LocalDate.of(2020, 1, 10);
        LocalDate datoTom = LocalDate.of(2020, 1, 20);

        int refusjonForPeriode = tilskuddForPeriode.beregnTilskuddForPeriode(månedslønn, datoFom, datoTom);
        assertEquals(6667, refusjonForPeriode);
    }

}
