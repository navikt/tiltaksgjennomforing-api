package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NyTilskuddForAvtalePeriode {

    private final static BigDecimal DAGER_I_MÅNED = BigDecimal.valueOf(30.4375);
    private final static int ANTALL_MÅNEDER_I_EN_PERIODE = 3;

    public static List<TilskuddPeriode> beregnTilskuddsperioderForAvtale(Integer sumLønnstilskuddPerMåned, LocalDate datoFraOgMed, LocalDate datoTilOgMed, Integer lonnstilskuddprosent, LocalDate datoForRedusertProsent, Integer sumLønnstilskuddPerMånedRedusert) {
        if (datoForRedusertProsent == null) {
            return lagDatoPar(datoFraOgMed, datoTilOgMed).stream().map(datoPar -> {
                Integer beløp = beløpForPeriode(datoPar.fra, datoPar.til, sumLønnstilskuddPerMåned);
                return new TilskuddPeriode(beløp, datoPar.fra, datoPar.til, lonnstilskuddprosent);
            }).collect(Collectors.toList());
        }

        List<TilskuddPeriode> tilskuddperioderFørRedusering = lagDatoPar(datoFraOgMed, datoForRedusertProsent.minusDays(1)).stream().map(datoPar -> {
            Integer beløp = beløpForPeriode(datoPar.fra, datoPar.til, sumLønnstilskuddPerMåned);
            return new TilskuddPeriode(beløp, datoPar.fra, datoPar.til, lonnstilskuddprosent);
        }).collect(Collectors.toList());

        List<TilskuddPeriode> tilskuddperioderEtterRedusering = lagDatoPar(datoForRedusertProsent, datoTilOgMed).stream().map(datoPar -> {
            Integer beløp = beløpForPeriode(datoPar.fra, datoPar.til, sumLønnstilskuddPerMånedRedusert);
            return new TilskuddPeriode(beløp, datoPar.fra, datoPar.til, lonnstilskuddprosent - 10);
        }).collect(Collectors.toList());

        ArrayList<TilskuddPeriode> tilskuddsperioder = new ArrayList<>();
        tilskuddsperioder.addAll(tilskuddperioderFørRedusering);
        tilskuddsperioder.addAll(tilskuddperioderEtterRedusering);
        return tilskuddsperioder;
    }

    private static Integer beløpForPeriode(LocalDate fra, LocalDate til, Integer sumLønnstilskuddPerMåned) {
        Period period = Period.between(fra, til.plusDays(1));
        Integer sumHeleMåneder = period.getMonths() * sumLønnstilskuddPerMåned;
        BigDecimal dagsats = new BigDecimal(sumLønnstilskuddPerMåned).divide(DAGER_I_MÅNED, RoundingMode.HALF_UP);
        Integer sumEnkeltdager = period.getDays() * dagsats.intValue();
        return sumHeleMåneder + sumEnkeltdager;
    }

    private static List<DatoPar> lagDatoPar(LocalDate datoFraOgMed, LocalDate datoTilOgMed) {
        List<LocalDate> startDatoer = datoFraOgMed.datesUntil(datoTilOgMed, Period.ofMonths(ANTALL_MÅNEDER_I_EN_PERIODE)).collect(Collectors.toList());
        ArrayList<DatoPar> datoPar = new ArrayList<>();
        for (int i = 0; i < startDatoer.size() - 1; i++) {
            LocalDate fra = startDatoer.get(i);
            LocalDate til = startDatoer.get(i + 1).minusDays(1);
            if (fra.getYear() != til.getYear()) {
                datoPar.add(new DatoPar(fra, fra.withMonth(12).withDayOfMonth(31)));
                datoPar.add(new DatoPar(til.withMonth(1).withDayOfMonth(1), til));
            } else {
                datoPar.add(new DatoPar(fra, til));
            }
        }
        datoPar.add(new DatoPar(startDatoer.get(startDatoer.size() - 1), datoTilOgMed));
        return datoPar;
    }

    @Value
    static class DatoPar {
        LocalDate fra;
        LocalDate til;
    }
}


// [[2020-11-10, 2021-02-10], [2021-02-10, 2021-05-10], [2021-08-10, datoTilOgMed]]