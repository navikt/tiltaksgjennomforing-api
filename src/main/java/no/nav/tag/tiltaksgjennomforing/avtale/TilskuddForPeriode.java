package no.nav.tag.tiltaksgjennomforing.avtale;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;

public class TilskuddForPeriode {

    private final static int dagerIMåned = 30;
    private final static int antallMånederIEnPeriode = 3;

    public List<TilskuddPeriode> beregnTilskuddForPeriode(final Integer lønnPrMåned, final LocalDate datoFraOgMed, final LocalDate datoTilOgMed){

        Period period = Period.between(datoFraOgMed, datoTilOgMed.plusDays(1));
        List<TilskuddPeriode> tilskuddPerioder = new ArrayList<>();
        LocalDate nyDatoFraOgMed = datoFraOgMed;
        LocalDate nyDatoTilOgMed;
        TilskuddPeriode nyTsPeriode;

        while(period.getMonths() >= antallMånederIEnPeriode){
            nyDatoTilOgMed = nyDatoFraOgMed.plusMonths(antallMånederIEnPeriode);
            nyTsPeriode = new TilskuddPeriode(lønnPrMåned * antallMånederIEnPeriode, nyDatoFraOgMed, nyDatoTilOgMed.minusDays(1), false);
            tilskuddPerioder.add(nyTsPeriode);
            nyDatoFraOgMed = nyDatoTilOgMed;
            period = period.minusMonths(antallMånederIEnPeriode);
        }

        if(!period.isZero()){
            nyTsPeriode = beregnTilsagnPåSistePeriode(lønnPrMåned, nyDatoFraOgMed, datoTilOgMed);
            tilskuddPerioder.add(nyTsPeriode);
        }
        return tilskuddPerioder;
    }

    long beregnetilskuddsperiode(LocalDate fraDato, LocalDate tilDato){
        return DAYS.between(fraDato, tilDato.plusDays(1));
    }

    private TilskuddPeriode beregnTilsagnPåSistePeriode(Integer lønnPrMåned, LocalDate datoFom, LocalDate datoTom){
        int antallDagerITilskuddsPerioden = dagerIMåned * antallMånederIEnPeriode;
        BigDecimal totalLønnIPerioden = BigDecimal.valueOf(lønnPrMåned).multiply(BigDecimal.valueOf(antallMånederIEnPeriode));
        BigDecimal dagslønn = totalLønnIPerioden.divide(BigDecimal.valueOf(antallDagerITilskuddsPerioden), MathContext.DECIMAL32).setScale(3, RoundingMode.HALF_UP);
        long dagerISisteTilskuddsPerioden = beregnetilskuddsperiode(datoFom, datoTom);
        Integer lønnISistePeriode = dagslønn.multiply(BigDecimal.valueOf(dagerISisteTilskuddsPerioden)).setScale(0, RoundingMode.HALF_UP).intValue();

        return new TilskuddPeriode(lønnISistePeriode, datoFom, datoTom, true);
    }
}
