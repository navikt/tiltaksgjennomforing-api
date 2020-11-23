package no.nav.tag.tiltaksgjennomforing.avtale;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;

public class TilskuddForAvtalePeriode {

    private final static BigDecimal dagerIMåned = BigDecimal.valueOf(30.4375);
    private final static int antallMånederIEnPeriode = 3;

    public static List<TilskuddPeriode> beregnTilskuddForEttÅr(final Integer lønnPrMåned, final LocalDate datoFraOgMed, final LocalDate datoTilOgMed){
        List<TilskuddPeriode> tilskuddPeriode = new ArrayList<>();
        LocalDate nyStartDato = datoFraOgMed;

        while (nyStartDato.getYear() != datoTilOgMed.getYear()) {
            tilskuddPeriode.addAll(beregnTilskuddForAvtalePeriode(lønnPrMåned, nyStartDato, LocalDate.of(nyStartDato.getYear(), 12, 31)));
            nyStartDato = nyStartDato.plusYears(1);
            nyStartDato = LocalDate.of(nyStartDato.getYear(), 1, 1);
        }

        tilskuddPeriode.addAll(beregnTilskuddForAvtalePeriode(lønnPrMåned, nyStartDato, datoTilOgMed));
        return tilskuddPeriode;
    }

    public static List<TilskuddPeriode> beregnTilskuddForAvtalePeriode(final Integer lønnPrMåned, final LocalDate datoFraOgMed, final LocalDate datoTilOgMed){

        Period period = Period.between(datoFraOgMed, datoTilOgMed.plusDays(1));
        List<TilskuddPeriode> tilskuddPerioder = new ArrayList<>();
        LocalDate nyDatoFraOgMed = datoFraOgMed;
        LocalDate nyDatoTilOgMed;
        TilskuddPeriode nyTsPeriode;

        if (period.getYears() > 0) {
           period = period.plusMonths(period.getYears() * 12);
           period = period.minusYears(1);
        }

        while(period.getMonths() >= antallMånederIEnPeriode){
            nyDatoTilOgMed = nyDatoFraOgMed.plusMonths(antallMånederIEnPeriode);
            nyTsPeriode = new TilskuddPeriode(lønnPrMåned * antallMånederIEnPeriode, nyDatoFraOgMed, nyDatoTilOgMed.minusDays(1));
            tilskuddPerioder.add(nyTsPeriode);
            nyDatoFraOgMed = nyDatoTilOgMed;
            period = period.minusMonths(antallMånederIEnPeriode);
        }

        if(!period.isZero()){
            nyTsPeriode = beregnTilsagnPåSistePeriode(lønnPrMåned, nyDatoFraOgMed, datoTilOgMed, period);
            tilskuddPerioder.add(nyTsPeriode);
        }
        return tilskuddPerioder;
    }

    static long beregnetilskuddsperiode(LocalDate fraDato, LocalDate tilDato){
        return DAYS.between(fraDato, tilDato.plusDays(1));
    }

    private static TilskuddPeriode beregnTilsagnPåSistePeriode(Integer lønnPrMåned, LocalDate datoFom, LocalDate datoTom, Period period){
        BigDecimal lønnPrMånedBD = BigDecimal.valueOf(lønnPrMåned);
        BigDecimal totalLønnIPerioden = BigDecimal.ZERO;

        while(period.getMonths() >= 1){
            totalLønnIPerioden = totalLønnIPerioden.add(lønnPrMånedBD);
            period = period.minusMonths(1);
        }

        BigDecimal lønnResterendeDager = beregnTilskuddPåDager(lønnPrMånedBD, period);
        totalLønnIPerioden = totalLønnIPerioden.add(lønnResterendeDager);
        Integer lønnISistePeriode = totalLønnIPerioden.setScale(0, RoundingMode.HALF_UP).intValue();
        return new TilskuddPeriode(lønnISistePeriode, datoFom, datoTom);
    }

    private static BigDecimal beregnTilskuddPåDager(BigDecimal lønnPrMåned, Period period) {
        BigDecimal restLønn = BigDecimal.ZERO;

        if(!period.isZero()) {
            BigDecimal dagsLønn = lønnPrMåned.divide(dagerIMåned, MathContext.DECIMAL32);
            restLønn = dagsLønn.multiply(BigDecimal.valueOf(period.getDays()));
        }
        return restLønn;
    }
}
