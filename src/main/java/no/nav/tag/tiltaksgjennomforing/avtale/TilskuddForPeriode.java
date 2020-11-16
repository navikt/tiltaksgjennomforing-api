package no.nav.tag.tiltaksgjennomforing.avtale;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;

public class TilskuddForPeriode {

    private final static BigDecimal dagerIMåned = BigDecimal.valueOf(30);

    public int beregnTilskuddForPeriode(BigDecimal sumlønnTilskudd, LocalDate datoFom, LocalDate datoTom){

        BigDecimal daysBetween = BigDecimal.valueOf(DAYS.between(datoFom, datoTom));

        BigDecimal heleMåneder = daysBetween.divide(dagerIMåned, RoundingMode.DOWN);
        BigDecimal dagerTilOvers = daysBetween.remainder(dagerIMåned);
        BigDecimal lønnUtoverHeleMåneder = sumlønnTilskudd.divide(dagerIMåned, MathContext.DECIMAL32).setScale(3, RoundingMode.HALF_DOWN);
        return (sumlønnTilskudd.multiply(heleMåneder)).add(lønnUtoverHeleMåneder.multiply(dagerTilOvers)).setScale(0, RoundingMode.HALF_DOWN).intValue();
    }
}

/*
 JANUAR     FEB    MAR    15 APRI
 20000  180000  200000    10000

TILSAGN: 70 0000

 */


//        inntekt.getOpptjeningsperiodeFom().datesUntil(inntekt.getOpptjeningsperiodeTom().plusDays(1)


//        List<LocalDate> dager = datoFom.datesUntil(datoTom.plusDays(1)).collect(Collectors.toList());
//
//        dager.stream().forEach(dag -> {
//
//
//
//        });