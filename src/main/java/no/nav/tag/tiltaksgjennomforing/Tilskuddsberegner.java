package no.nav.tag.tiltaksgjennomforing;

import java.math.BigDecimal;
import java.math.MathContext;

public class Tilskuddsberegner {

    public static BigDecimal beregnferiepenger(BigDecimal månedslønn, BigDecimal feriepengesats){
        BigDecimal svar = månedslønn.multiply(feriepengesats);
        return svar.round(MathContext.DECIMAL32); //(new MathContext(0));
    }

    public static BigDecimal summerLonnFeriePensjon(BigDecimal månedslonn, BigDecimal feriepenger,  BigDecimal obligTjenestepensjon){
        return månedslonn.add(feriepenger).add(obligTjenestepensjon).round(MathContext.UNLIMITED);
    }

}


//export const sumLonnFeriePensjon = (manedslonn?: number, feriepenger?: number, obligTjenestepensjon?: number) => {
//        return manedslonn && feriepenger && obligTjenestepensjon
//        ? Math.round(manedslonn + feriepenger + obligTjenestepensjon)
//        : 0;
//        };