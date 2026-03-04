package no.nav.tag.tiltaksgjennomforing.utils;

import lombok.Value;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;
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
        if (split == null) {
            return List.of(this);
        }

        List<LocalDate> datoerSomFinnesIPerioden = Stream.of(split).filter(this::inneholder).toList();
        if (datoerSomFinnesIPerioden.isEmpty()) {
            return List.of(this);
        }

        List<LocalDate> datoerMedStartOgSlutt =  Stream.of(List.of(start), datoerSomFinnesIPerioden, List.of(slutt))
            .flatMap(Collection::stream)
            .sorted()
            .distinct()
            .toList();

        return IntStream.range(0, datoerMedStartOgSlutt.size() - 1)
            .mapToObj(i -> {
                LocalDate periodeStart = datoerMedStartOgSlutt.get(i);
                LocalDate periodeSlutt= datoerMedStartOgSlutt.get(i + 1);
                return new Periode(periodeStart, periodeSlutt.equals(slutt) ? periodeSlutt : periodeSlutt.minusDays(1));
            })
            .toList();
    }

    public static Periode av(LocalDate start, LocalDate slutt) {
        return new Periode(start, slutt);
    }

}
