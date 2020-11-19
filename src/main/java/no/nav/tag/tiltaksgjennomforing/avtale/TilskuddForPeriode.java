package no.nav.tag.tiltaksgjennomforing.avtale;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;

public class TilskuddForPeriode {

    private final static int dagerIMåned = 30;
    private final static int antallMånederIEnPeriode = 3;

    public List<TilskuddPeriode> beregnTilskuddForPeriode(Integer lønnPrMåned, LocalDate datoFom, LocalDate datoTom){

//        return opprettTilskuddsPerioder(lønnPrMåned, datoFom, datoTom);

        Period period = Period.between(datoFom, datoTom);
        List<TilskuddPeriode> tilskuddPerioder = new ArrayList<>();
        LocalDate nyDatoFom = datoFom;
        LocalDate nyDatoTom;
        TilskuddPeriode nyTsPeriode;

        while(period.getMonths() >= 3){
            nyDatoTom = nyDatoFom.plusMonths(antallMånederIEnPeriode);
            nyTsPeriode = new TilskuddPeriode(lønnPrMåned * antallMånederIEnPeriode, nyDatoFom, nyDatoTom, false);
            tilskuddPerioder.add(nyTsPeriode);
            nyDatoFom = nyTsPeriode.getDatoTom().plusDays(1);
            period = period.minusMonths(antallMånederIEnPeriode);
        }

        if(!period.isZero()){
            nyDatoTom = nyDatoFom.plus(period);
            nyTsPeriode = beregnTilsagnPåSistePeriode(lønnPrMåned, nyDatoFom, nyDatoTom);
            tilskuddPerioder.add(nyTsPeriode);
        }
        return tilskuddPerioder;
    }

    private TilskuddPeriode beregnTilsagnPåSistePeriode(Integer lønnPrMåned, LocalDate datoFom, LocalDate datoTom){ //TODO Avklare beregning av resterende periode: Skal vi ta  med helgene?

//        case: periode = 1 jan - 15 feb:

//        1. Finn antall_dager i hele perioden (1 jan - 31 mars)
        int antallDagerIHelePerioden = dagerIMåned * antallMånederIEnPeriode;

//        2. Finn lønn for hele perioden (1 jan - 31 mars): lønnPrMåned * 3,
        BigDecimal totalLønnIPerioden = BigDecimal.valueOf(lønnPrMåned).multiply(BigDecimal.valueOf(antallMånederIEnPeriode));

//        3. Del max periodelønn (1 jan - 31 mars) => dagslønn
//        int dagslønn = totalLønnIPerioden / antallDagerIHelePerioden;

        BigDecimal dagslønn = totalLønnIPerioden.divide(BigDecimal.valueOf(antallDagerIHelePerioden), MathContext.DECIMAL32).setScale(3, RoundingMode.HALF_UP);


//        4. Finn antall_dager i perioden 1 jan - 15 feb
        long dagerISistePerioden = DAYS.between(datoFom, datoTom.plusDays(1));

//        5. multipliser dagslønn med antall antall_dager fra 3.
        Integer lønnISistePeriode = dagslønn.multiply(BigDecimal.valueOf(dagerISistePerioden)).setScale(0, RoundingMode.HALF_UP).intValue();

        return new TilskuddPeriode(lønnISistePeriode, datoFom, datoTom, true);
    }

    private boolean erEnHelMåned(LocalDate datoFom, LocalDate datoTom) {
        return datoFom.getDayOfMonth() == 1 && datoTom.equals(YearMonth.from(datoTom).atEndOfMonth());
    }

}






//        BigDecimal dagLønn = månedsBeløp.divide(dagerIMåned, MathContext.DECIMAL32).setScale(3, RoundingMode.HALF_DOWN);
//        Integer beløp = dagLønn.multiply(BigDecimal.valueOf(dagerIPerioden)).intValue();

