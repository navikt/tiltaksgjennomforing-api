package no.nav.tag.tiltaksgjennomforing.avtale;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public class TilskuddForAvtalePeriode {

    private final static BigDecimal DAGER_I_MÅNED = BigDecimal.valueOf(30.4375);
    private final static int ANTALL_MÅNEDER_I_EN_PERIODE = 3;

    public static List<TilskuddPeriode> beregnTilskuddsperioderForAvtale(final Integer lønnPrMåned, final LocalDate datoFraOgMed, final LocalDate datoTilOgMed, final Integer lonnstilskuddprosent, LocalDate datoForRedusertProsent, Integer sumLønnstilskuddRedusert){
        List<TilskuddPeriode> tilskuddPerioder = new ArrayList<>();
        LocalDate nyStartDato = datoFraOgMed;
        nyStartDato = beregnTilskuddsPerioderForHvertHeleÅr(lønnPrMåned, datoTilOgMed, tilskuddPerioder, nyStartDato, lonnstilskuddprosent, datoForRedusertProsent, sumLønnstilskuddRedusert);
        tilskuddPerioder.addAll(beregnTilskuddForPeriode(lønnPrMåned, nyStartDato, datoTilOgMed, lonnstilskuddprosent, datoForRedusertProsent, sumLønnstilskuddRedusert));
        return tilskuddPerioder;
    }

    private static LocalDate beregnTilskuddsPerioderForHvertHeleÅr(Integer lønnPrMåned, LocalDate datoTilOgMed, List<TilskuddPeriode> tilskuddPeriode, LocalDate nyStartDato, Integer lonnstilskuddprosent, LocalDate datoForRedusertProsent, Integer sumLønnstilskuddRedusert) {
        while (nyStartDato.getYear() != datoTilOgMed.getYear()) {
            tilskuddPeriode.addAll(beregnTilskuddForPeriode(lønnPrMåned, nyStartDato, LocalDate.of(nyStartDato.getYear(), 12, 31), lonnstilskuddprosent, datoForRedusertProsent, sumLønnstilskuddRedusert));
            nyStartDato = LocalDate.of(nyStartDato.getYear() + 1, 1, 1);
        }
        return nyStartDato;
    }

    private static List<TilskuddPeriode> beregnTilskuddForPeriode(final Integer lønnPrMåned, final LocalDate datoFraOgMed, final LocalDate datoTilOgMed, final Integer lonnstilskuddprosent, LocalDate datoForRedusertProsent, Integer sumLønnstilskuddRedusert){
        Period period = lagPeriod(datoFraOgMed, datoTilOgMed);
        List<TilskuddPeriode> tilskuddPerioder = new ArrayList<>();
        LocalDate nyDatoFraOgMed = datoFraOgMed;
        LocalDate nyDatoTilOgMed;
        TilskuddPeriode nyTsPeriode;

        while(period.getMonths() >= ANTALL_MÅNEDER_I_EN_PERIODE){
            nyDatoTilOgMed = nyDatoFraOgMed.plusMonths(ANTALL_MÅNEDER_I_EN_PERIODE);
            nyTsPeriode = new TilskuddPeriode(lønnPrMåned * ANTALL_MÅNEDER_I_EN_PERIODE, nyDatoFraOgMed, nyDatoTilOgMed.minusDays(1), lonnstilskuddprosent);
            tilskuddPerioder.add(nyTsPeriode);
            nyDatoFraOgMed = nyDatoTilOgMed;
            period = period.minusMonths(ANTALL_MÅNEDER_I_EN_PERIODE);
        }

        if(!period.isZero()){
            nyTsPeriode = beregnTilsagnPåSistePeriode(BigDecimal.valueOf(lønnPrMåned), nyDatoFraOgMed, datoTilOgMed, period, lonnstilskuddprosent);
            tilskuddPerioder.add(nyTsPeriode);
        }
        return tilskuddPerioder;
    }

    private static TilskuddPeriode beregnTilsagnPåSistePeriode(BigDecimal lønnPrMåned, LocalDate datoFom, LocalDate datoTom, Period period, Integer lonnstilskuddprosent){
        BigDecimal totalLønnIPerioden = BigDecimal.ZERO;

        while(period.getMonths() >= 1){
            totalLønnIPerioden = totalLønnIPerioden.add(lønnPrMåned);
            period = period.minusMonths(1);
        }

        BigDecimal lønnResterendeDager = beregnTilskuddPåDager(lønnPrMåned, period);
        totalLønnIPerioden = totalLønnIPerioden.add(lønnResterendeDager);
        Integer lønnISistePeriode = totalLønnIPerioden.setScale(0, RoundingMode.HALF_UP).intValue();
        return new TilskuddPeriode(lønnISistePeriode, datoFom, datoTom, lonnstilskuddprosent);
    }

    private static BigDecimal beregnTilskuddPåDager(BigDecimal lønnPrMåned, Period period) {
        BigDecimal restLønn = BigDecimal.ZERO;

        if(!period.isZero()) {
            BigDecimal dagsLønn = lønnPrMåned.divide(DAGER_I_MÅNED, MathContext.DECIMAL32);
            restLønn = dagsLønn.multiply(BigDecimal.valueOf(period.getDays()));
        }
        return restLønn;
    }

    private static Period lagPeriod(LocalDate fom, LocalDate tom){
        Period period = Period.between(fom, tom.plusDays(1));
        if (period.getYears() > 0) {
            period = period.plusMonths(period.getYears() * 12);
            period = period.minusYears(1);
        }
        return period;
    }
}
