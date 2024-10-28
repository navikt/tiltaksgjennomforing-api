package no.nav.tag.tiltaksgjennomforing.satser;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

public class Sats {

    public static final Sats INKLUDERINGSTILSKUDD_SATS = new Sats(List.of(
            new SatsPeriodeData(
                    149_100,
                    LocalDate.of(2024, 1, 1),
                    null
            ),
            new SatsPeriodeData(
                    143_900,
                    LocalDate.of(2000, 1, 1),
                    LocalDate.of(2023, 12, 31)
            )
    ));

    public static final Sats VTAO_SATS = new Sats(List.of(
            new SatsPeriodeData(
                    6_808,
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 12, 31)
            ), new SatsPeriodeData(
                    6_428,
                    LocalDate.of(2023, 1, 1),
                    LocalDate.of(2023, 12, 31)
            ), new SatsPeriodeData(
                    6_241,
                    LocalDate.of(2022, 1, 1),
                    LocalDate.of(2022, 12, 31)
            )
    ));

    private final NavigableMap<LocalDate, Integer> satsePerioder;

    Sats(List<SatsPeriodeData> satserEntitet) {
        this.satsePerioder = lagPeriodesett(satserEntitet);
    }

    public NavigableMap<LocalDate, Integer> getSatsePerioder() {
        return satsePerioder;
    }

    public Integer hentGjeldendeSats(LocalDate dato) {
        var entry = satsePerioder.floorEntry(dato);
        return entry == null ? null : entry.getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sats sats = (Sats) o;
        return Objects.equals(satsePerioder, sats.satsePerioder);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(satsePerioder);
    }

    @Override
    public String toString() {
        var string = new StringBuilder("Sats:\n");
        var satseperiodeEntryset = new ArrayList<>(this.satsePerioder.entrySet());
        for (int i = 0; i < satseperiodeEntryset.size() - 1; i++) {
            string.append(satseperiodeEntryset.get(i).getKey());
            string.append("\t");
            string.append(satseperiodeEntryset.get(i + 1).getKey().minusDays(1));
            string.append(":\t");
            string.append(satseperiodeEntryset.get(i).getValue());
            string.append("\n");
        }
        if (!satseperiodeEntryset.isEmpty()) {
            var sistePeriode = satseperiodeEntryset.getLast();
            string.append(sistePeriode.getKey());
            string.append("\t");
            string.append(LocalDate.MAX);
            string.append(":\t");
            string.append(sistePeriode.getValue());
            string.append("\n");
        }
        return string.toString();
    }

    static NavigableMap<LocalDate, Integer> lagPeriodesett(List<SatsPeriodeData> satserEntitet) {
        NavigableMap<LocalDate, Integer> m = new TreeMap<>();

        satserEntitet.stream()
                .sorted(Comparator.comparing(SatsPeriodeData::gyldigFraOgMed))
                .forEach(sats -> {
                    m.put(sats.gyldigFraOgMed(), sats.satsVerdi());
                    if (sats.gyldigTilOgMed() != null) {
                        m.put(sats.gyldigTilOgMed().plusDays(1), null);
                    }
                });

        return m;
    }
}
