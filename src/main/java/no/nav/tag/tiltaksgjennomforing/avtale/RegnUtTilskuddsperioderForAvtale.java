package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.experimental.UtilityClass;
import no.nav.tag.tiltaksgjennomforing.utils.Periode;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class RegnUtTilskuddsperioderForAvtale {

    private final static BigDecimal DAGER_I_MÅNED = new BigDecimal("30.4375");
    private final static int ANTALL_MÅNEDER_I_EN_PERIODE = 3;

    public static List<TilskuddPeriode> beregnTilskuddsperioderForAvtale(Integer sumLønnstilskuddPerMåned, LocalDate datoFraOgMed, LocalDate datoTilOgMed, Integer lonnstilskuddprosent, LocalDate datoForRedusertProsent, Integer sumLønnstilskuddPerMånedRedusert) {
        if (datoForRedusertProsent == null) {
            return lagPeriode(datoFraOgMed, datoTilOgMed).stream().map(datoPar -> {
                Integer beløp = beløpForPeriode(datoPar.getStart(), datoPar.getSlutt(), sumLønnstilskuddPerMåned);
                return new TilskuddPeriode(beløp, datoPar.getStart(), datoPar.getSlutt(), lonnstilskuddprosent);
            }).collect(Collectors.toList());
        } else {
            List<TilskuddPeriode> tilskuddperioderFørRedusering = lagPeriode(datoFraOgMed, datoForRedusertProsent.minusDays(1)).stream().map(datoPar -> {
                Integer beløp = beløpForPeriode(datoPar.getStart(), datoPar.getSlutt(), sumLønnstilskuddPerMåned);
                return new TilskuddPeriode(beløp, datoPar.getStart(), datoPar.getSlutt(), lonnstilskuddprosent);
            }).collect(Collectors.toList());

            List<TilskuddPeriode> tilskuddperioderEtterRedusering = lagPeriode(datoForRedusertProsent, datoTilOgMed).stream().map(datoPar -> {
                Integer beløp = beløpForPeriode(datoPar.getStart(), datoPar.getSlutt(), sumLønnstilskuddPerMånedRedusert);
                return new TilskuddPeriode(beløp, datoPar.getStart(), datoPar.getSlutt(), lonnstilskuddprosent - 10);
            }).collect(Collectors.toList());

            ArrayList<TilskuddPeriode> tilskuddsperioder = new ArrayList<>();
            tilskuddsperioder.addAll(tilskuddperioderFørRedusering);
            tilskuddsperioder.addAll(tilskuddperioderEtterRedusering);
            return tilskuddsperioder;
        }

    }

    public static Integer beløpForPeriode(LocalDate datoFraOgMed, LocalDate datoTilOgMed, LocalDate datoForRedusertProsent, Integer sumLønnstilskuddPerMåned, Integer sumLønnstilskuddPerMånedRedusert) {
        if (datoForRedusertProsent == null || datoTilOgMed.isBefore(datoForRedusertProsent)) {
            return beløpForPeriode(datoFraOgMed, datoTilOgMed, sumLønnstilskuddPerMåned);
        } else {
            return beløpForPeriode(datoFraOgMed, datoTilOgMed, sumLønnstilskuddPerMånedRedusert);
        }
    }

    public static Integer beløpForPeriode(LocalDate fra, LocalDate til, Integer sumLønnstilskuddPerMåned) {
        Period period = Period.between(fra, til.plusDays(1));
        Integer sumHeleMåneder = period.getMonths() * sumLønnstilskuddPerMåned;
        BigDecimal dagsats = new BigDecimal(sumLønnstilskuddPerMåned).divide(DAGER_I_MÅNED, RoundingMode.HALF_UP);
        Integer sumEnkeltdager = dagsats.multiply(BigDecimal.valueOf(period.getDays()), MathContext.UNLIMITED).setScale(0, RoundingMode.HALF_UP).intValue();
        return sumHeleMåneder + sumEnkeltdager;
    }

    private static List<Periode> lagPeriode(LocalDate datoFraOgMed, LocalDate datoTilOgMed) {
        if (datoFraOgMed.isAfter(datoTilOgMed)) {
            return List.of();
        }
        List<LocalDate> startDatoer = datoFraOgMed.datesUntil(datoTilOgMed.plusDays(1), Period.ofMonths(ANTALL_MÅNEDER_I_EN_PERIODE)).collect(Collectors.toList());
        ArrayList<Periode> datoPar = new ArrayList<>();
        for (int i = 0; i < startDatoer.size() - 1; i++) {
            LocalDate fra = startDatoer.get(i);
            LocalDate til = startDatoer.get(i + 1).minusDays(1);
            datoPar.addAll(splittHvisNyttÅr(fra, til));
        }
        datoPar.addAll(splittHvisNyttÅr(startDatoer.get(startDatoer.size() - 1), datoTilOgMed));
        return datoPar;
    }

    private static List<Periode> splittHvisNyttÅr (LocalDate fraDato, LocalDate tilDato) {
        if (fraDato.getYear() != tilDato.getYear()) {
            Periode datoPar1 = new Periode(fraDato, fraDato.withMonth(12).withDayOfMonth(31));
            Periode datoPar2 = new Periode(tilDato.withMonth(1).withDayOfMonth(1), tilDato);
            return List.of(datoPar1, datoPar2);
        } else {
            return List.of(new Periode(fraDato, tilDato));
        }
    }
}