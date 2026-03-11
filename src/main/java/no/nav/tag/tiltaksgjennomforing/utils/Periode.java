package no.nav.tag.tiltaksgjennomforing.utils;

import lombok.Value;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@Value
public class Periode {
    private final LocalDate start;
    private final LocalDate slutt;

    public Periode(LocalDate start, LocalDate slutt) {
        if (start.isAfter(slutt)) {
            throw new IllegalArgumentException("Startdato må være før eller lik som sluttdato");
        }
        this.start = start;
        this.slutt = slutt;
    }

    public boolean inneholder(LocalDate dato) {
        return !dato.isBefore(start) && !dato.isAfter(slutt);
    }

    public List<Periode> split(LocalDate ...split) {
        return split(Arrays.stream(split).toList());
    }

    public List<Periode> split(List<LocalDate> split) {
        if (split == null || split.isEmpty()) {
            return List.of(this);
        }

        List<LocalDate> datoerSomFinnesIPerioden = split.stream()
            .filter(dato -> this.inneholder(dato) && !dato.isEqual(start))
            .distinct()
            .toList();

        if (datoerSomFinnesIPerioden.isEmpty()) {
            return List.of(this);
        }

        List<LocalDate> datoerMedStartOgSlutt =  Stream.of(List.of(start), datoerSomFinnesIPerioden, List.of(slutt))
            .flatMap(Collection::stream)
            .sorted()
            .toList();

        return IntStream.range(0, datoerMedStartOgSlutt.size() - 1)
            .mapToObj(i -> {
                boolean erSisteElement = i == datoerMedStartOgSlutt.size() - 2;

                LocalDate periodeStart = datoerMedStartOgSlutt.get(i);
                LocalDate periodeSlutt= datoerMedStartOgSlutt.get(i + 1);

                return new Periode(
                    periodeStart,
                    erSisteElement ? periodeSlutt : periodeSlutt.minusDays(1)
                );
            })
            .toList();
    }

    public static Periode av(LocalDate start, LocalDate slutt) {
        return new Periode(start, slutt);
    }

    public List<Periode> splitPerMnd() {
        YearMonth startMonth = YearMonth.from(start);
        YearMonth sluttMonth = YearMonth.from(slutt);
        long monthsBetween = ChronoUnit.MONTHS.between(startMonth, sluttMonth);

        return LongStream.rangeClosed(0, monthsBetween)
            .mapToObj(startMonth::plusMonths)
            .map(month -> {
                LocalDate fra = month.equals(startMonth) ? start : month.atDay(1);
                LocalDate til = month.equals(sluttMonth) ? slutt : month.atEndOfMonth();
                return new Periode(fra, til);
            })
            .toList();
    }


}
