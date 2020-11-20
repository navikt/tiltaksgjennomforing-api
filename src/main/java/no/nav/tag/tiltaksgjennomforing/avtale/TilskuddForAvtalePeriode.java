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

    private final static int dagerIMåned = 30; //TODO 30,4375
    private final static int antallMånederIEnPeriode = 3;

    public List<TilskuddPeriode> beregnTilskuddForAvtalePeriode(final Integer lønnPrMåned, final LocalDate datoFraOgMed, final LocalDate datoTilOgMed){

        Period period = Period.between(datoFraOgMed, datoTilOgMed.plusDays(1));
        List<TilskuddPeriode> tilskuddPerioder = new ArrayList<>();
        LocalDate nyDatoFraOgMed = datoFraOgMed;
        LocalDate nyDatoTilOgMed;
        TilskuddPeriode nyTsPeriode;

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

    long beregnetilskuddsperiode(LocalDate fraDato, LocalDate tilDato){
        return DAYS.between(fraDato, tilDato.plusDays(1));
    }

    private TilskuddPeriode beregnTilsagnPåSistePeriode(Integer lønnPrMåned, LocalDate datoFom, LocalDate datoTom, Period period){
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

    private BigDecimal beregnTilskuddPåDager(BigDecimal lønnPrMåned, Period period) {
        BigDecimal restLønn = BigDecimal.ZERO;

        if(!period.isZero()) {
            BigDecimal dagsLønn = lønnPrMåned.divide(BigDecimal.valueOf(dagerIMåned), MathContext.DECIMAL32);
            restLønn = dagsLønn.multiply(BigDecimal.valueOf(period.getDays()));
        }
        return restLønn;
    }
}
